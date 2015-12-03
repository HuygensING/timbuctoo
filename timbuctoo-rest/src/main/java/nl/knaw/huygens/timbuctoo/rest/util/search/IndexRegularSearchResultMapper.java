package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.RegularSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.rest.util.RangeHelper;
import nl.knaw.huygens.timbuctoo.search.FullTextSearchFieldFinder;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * A search result mapper that retrieves the information from the execute rather that from the database.
 */
public class IndexRegularSearchResultMapper extends RegularSearchResultMapper {
  private static final Logger LOG = LoggerFactory.getLogger(IndexRegularSearchResultMapper.class);
  private DomainEntityDTOListFactory domainEntityDTOListFactory;

  @Inject
  public IndexRegularSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, FullTextSearchFieldFinder fullTextSearchFieldFinder, VRECollection vreCollection) {
    this(repository, sortableFieldFinder, hateoasURICreator, fullTextSearchFieldFinder, vreCollection, new RangeHelper(), new DomainEntityDTOListFactory());
  }

  IndexRegularSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, FullTextSearchFieldFinder fullTextSearchFieldFinder, VRECollection vreCollection, RangeHelper rangeHelper) {
    super(repository, sortableFieldFinder, hateoasURICreator, fullTextSearchFieldFinder, vreCollection, rangeHelper);
  }

  public IndexRegularSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator uriCreator, FullTextSearchFieldFinder fullTextSearchFieldFinder, VRECollection vreCollection, RangeHelper rangeHelper, DomainEntityDTOListFactory domainEntityDTOListFactory) {
    super(repository, sortableFieldFinder, uriCreator, fullTextSearchFieldFinder, vreCollection, rangeHelper);
    this.domainEntityDTOListFactory = domainEntityDTOListFactory;
  }

  @Override
  public <T extends DomainEntity> RegularSearchResultDTO create(Class<T> type, SearchResult searchResult, int start, int rows, String version) {
    List<String> ids = getIds(searchResult);
    int numFound = ids.size();
    LOG.debug("num found {}", numFound);
    int normalizedStart = mapToRange(start, 0, numFound);
    int normalizedRows = mapToRange(rows, 0, numFound - normalizedStart);
    int end = normalizedStart + normalizedRows;

    List<String> idsToRetrieve = ids.subList(normalizedStart, end);
    LOG.debug("number of ids to retrieve {}", idsToRetrieve.size());

    List<DomainEntityDTO> refs = null;
    try {
      List<Map<String, Object>> rawData = vreCollection.getVREById(searchResult.getVreId()).getRawDataFor(type, idsToRetrieve, searchResult.getSort());
      LOG.debug("number of items found in execute {}", rawData.size());
      refs = domainEntityDTOListFactory.createFor(type, rawData);
    } catch (SearchException | NotInScopeException | SearchResultCreationException e) {
      throw new RuntimeException(e); // FIXME: Hack to inform the client the search went wrong, and not change the API
    }

      String queryId = searchResult.getId();

      RegularSearchResultDTO dto = new RegularSearchResultDTO();

      dto.setRows(normalizedRows);
      dto.setStart(normalizedStart);
      dto.setIds(ids);
      dto.setNumFound(numFound);
      dto.setRefs(refs);
      dto.setSortableFields(sortableFieldFinder.findFields(type));
      dto.setTerm(searchResult.getTerm());
      dto.setFacets(searchResult.getFacets());
      dto.setFullTextSearchFields(fullTextSearchFieldFinder.findFields(type));

      dto.setNextLink(hateoasURICreator.createNextResultsAsString(normalizedStart, rows, numFound, queryId, version));
      dto.setPrevLink(hateoasURICreator.createPrevResultsAsString(normalizedStart, rows, queryId, version));

      return dto;
    }
  }
