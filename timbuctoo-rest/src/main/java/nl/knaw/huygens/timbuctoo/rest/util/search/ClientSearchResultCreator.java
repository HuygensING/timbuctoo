package nl.knaw.huygens.timbuctoo.rest.util.search;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.ClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.resources.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public abstract class ClientSearchResultCreator {

  private static final Logger LOG = LoggerFactory.getLogger(RegularClientSearchResultCreator.class);

  protected final Repository repository;
  protected final SortableFieldFinder sortableFieldFinder;
  protected final HATEOASURICreator hateoasURICreator;

  public ClientSearchResultCreator(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator) {
    this.repository = repository;
    this.sortableFieldFinder = sortableFieldFinder;
    this.hateoasURICreator = hateoasURICreator;
  }

  public abstract <T extends DomainEntity> ClientSearchResult create(Class<T> type, SearchResult searchResult, int start, int rows);

  protected <T extends DomainEntity> List<T> retrieveEntities(Class<T> type, List<String> ids) {
    // Retrieve one-by-one to retain ordering
    List<T> entities = Lists.newArrayList();
    for (String id : ids) {
      T entity = repository.getEntity(type, id);
      if (entity != null) {
        entities.add(entity);
      } else {
        LOG.error("Failed to retrieve {} - {}", type, id);
      }
    }
    return entities;
  }

  protected void setNextLink(int start, int rows, ClientSearchResult clientSearchResult, int numFound, int end, String queryId) {
    if (end < numFound) {
      String next = hateoasURICreator.createHATEOASURIAsString(start + rows, rows, queryId);
      clientSearchResult.setNextLink(next);
    }
  }

  protected void setPreviousLink(int start, int rows, ClientSearchResult clientSearchResult, String queryId) {
    if (start > 0) {
      int prevStart = Math.max(start - rows, 0);
      String prev = hateoasURICreator.createHATEOASURIAsString(prevStart, rows, queryId);
      clientSearchResult.setPrevLink(prev.toString());
    }
  }

  protected List<String> getIds(SearchResult searchResult) {
    return searchResult.getIds() != null ? searchResult.getIds() : Lists.<String> newArrayList();
  }

}
