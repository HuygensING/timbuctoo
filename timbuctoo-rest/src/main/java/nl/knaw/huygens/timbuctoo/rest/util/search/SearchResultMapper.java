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
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResultDTO;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.rest.util.RangeHelper;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class SearchResultMapper {

  private static final Logger LOG = LoggerFactory.getLogger(SearchResultMapper.class);

  protected final Repository repository;
  protected final SortableFieldFinder sortableFieldFinder;
  protected final HATEOASURICreator hateoasURICreator;
  protected final VRECollection vreCollection;
  private final RangeHelper rangeHelper;

  public SearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, VRECollection vreCollection) {
    this(repository, sortableFieldFinder, hateoasURICreator, vreCollection, new RangeHelper());
  }

  SearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, VRECollection vreCollection, RangeHelper rangeHelper) {
    this.repository = repository;
    this.sortableFieldFinder = sortableFieldFinder;
    this.hateoasURICreator = hateoasURICreator;
    this.vreCollection = vreCollection;
    this.rangeHelper = rangeHelper;
  }

  public abstract <T extends DomainEntity> SearchResultDTO create(Class<T> type, SearchResult searchResult, int start, int rows, String version);

  protected <T extends DomainEntity> List<T> retrieveEntities(Class<T> type, List<String> ids) {
    // Retrieve one-by-one to retain ordering
    List<T> entities = Lists.newArrayList();
    for (String id : ids) {
      T entity = repository.getEntityOrDefaultVariation(type, id);
      if (entity != null) {
        entities.add(entity);
      } else {
        LOG.error("Failed to retrieve {} - {}", type, id);
      }
    }
    return entities;
  }

  protected <T extends DomainEntity> List<T> retrieveEntitiesWithRelationsAndDerivedProperties(Class<T> type, List<String> ids, String vreId) {
    // Retrieve one-by-one to retain ordering
    List<T> entities = Lists.newArrayList();
    VRE vre = vreCollection.getVREById(vreId);
    for (String id : ids) {
      T entity = repository.getEntityOrDefaultVariationWithRelations(type, id);
      repository.addDerivedProperties(vre, entity);
      if (entity != null) {
        entities.add(entity);
      } else {
        LOG.error("Failed to retrieve {} - {}", type, id);
      }
    }
    return entities;
  }

  protected void setNextLink(int start, int rows, SearchResultDTO clientSearchResult, int numFound, int end, String queryId, String version) {
    String next = hateoasURICreator.createNextResultsAsString(start, rows, numFound, queryId, version);
    clientSearchResult.setNextLink(next);
  }

  protected void setPreviousLink(int start, int rows, SearchResultDTO clientSearchResult, String queryId, String version) {
    clientSearchResult.setPrevLink(hateoasURICreator.createPrevResultsAsString(start, rows,queryId,version));
  }

  protected List<String> getIds(SearchResult searchResult) {
    return searchResult.getIds() != null ? searchResult.getIds() : Lists.<String>newArrayList();
  }

  protected int mapToRange(int start, int minValue, int maxValue) {
    return rangeHelper.mapToRange(start, minValue, maxValue);
  }
}
