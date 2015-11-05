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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.RegularSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.rest.util.RangeHelper;
import nl.knaw.huygens.timbuctoo.search.FullTextSearchFieldFinder;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

import java.util.List;

public class RegularSearchResultMapper extends SearchResultMapper {

  protected final FullTextSearchFieldFinder fullTextSearchFieldFinder;

  @Inject
  public RegularSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, FullTextSearchFieldFinder fullTextSearchFieldFinder,
      VRECollection vreCollection) {
    super(repository, sortableFieldFinder, hateoasURICreator, vreCollection);
    this.fullTextSearchFieldFinder = fullTextSearchFieldFinder;
  }

  RegularSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, FullTextSearchFieldFinder fullTextSearchFieldFinder,
                            VRECollection vreCollection, RangeHelper rangeHelper){
    super(repository, sortableFieldFinder, hateoasURICreator, vreCollection, rangeHelper);
    this.fullTextSearchFieldFinder = fullTextSearchFieldFinder;
  }

  @Override
  public <T extends DomainEntity> RegularSearchResultDTO create(Class<T> type, SearchResult searchResult, int start, int rows, String version) {
    RegularSearchResultDTO dto = new RegularSearchResultDTO();

    List<String> ids = getIds(searchResult);
    int numFound = ids.size();
    int normalizedStart = mapToRange(start, 0, numFound);
    int normalizedRows = mapToRange(rows, 0, numFound - normalizedStart);
    int end = normalizedStart + normalizedRows;

    List<String> idsToRetrieve = ids.subList(normalizedStart, end);
    List<T> results = retrieveEntitiesWithRelationsAndDerivedProperties(type, idsToRetrieve, searchResult.getVreId());

    String queryId = searchResult.getId();

    dto.setRows(normalizedRows);
    dto.setStart(normalizedStart);
    dto.setIds(ids);
    dto.setResults(results);
    dto.setNumFound(numFound);
    dto.setRefs(createRefs(type, results));
    dto.setSortableFields(sortableFieldFinder.findFields(type));
    dto.setTerm(searchResult.getTerm());
    dto.setFacets(searchResult.getFacets());
    dto.setFullTextSearchFields(fullTextSearchFieldFinder.findFields(type));

    setPreviousLink(normalizedStart, rows, dto, queryId, version);
    setNextLink(start, rows, dto, numFound, end, queryId, version);

    return dto;
  }

  private <T extends DomainEntity> List<DomainEntityDTO> createRefs(Class<T> type, List<T> entities) {
    String itype = TypeNames.getInternalName(type);
    String xtype = TypeNames.getExternalName(type);
    List<DomainEntityDTO> list = Lists.newArrayListWithCapacity(entities.size());
    for (DomainEntity entity : entities) {
      list.add(new DomainEntityDTO(itype, xtype, entity));
      // TODO eliminate this, once results are no longer part of the representation
      entity.clearRelations();
    }
    return list;
  }

}
