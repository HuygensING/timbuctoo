package nl.knaw.huygens.timbuctoo.rest.resources;

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
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;

import com.google.inject.Inject;

public class RelationClientSearchResultCreator extends ClientSearchResultCreator {

  private final ClientRelationRepresentationCreator clientRelationRepresentationCreator;

  @Inject
  public RelationClientSearchResultCreator(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator,
      ClientRelationRepresentationCreator clientRelationRepresentationCreator) {
    super(repository, sortableFieldFinder, hateoasURICreator);
    this.clientRelationRepresentationCreator = clientRelationRepresentationCreator;
  }

  @Override
  public <T extends DomainEntity> RelationClientSearchResult create(Class<T> type, SearchResult searchResult, int start, int rows) {
    RelationClientSearchResult clientSearchResult = new RelationClientSearchResult();

    String queryId = searchResult.getId();
    List<String> ids = getIds(searchResult);
    int numFound = ids.size();
    int normalizedStart = mapToRange(start, 0, numFound);
    int normalizedRows = mapToRange(rows, 0, numFound - start);
    int end = normalizedStart + normalizedRows;

    List<String> idsToRetrieve = ids.subList(normalizedStart, end);
    List<T> results = retrieveEntities(type, idsToRetrieve);

    clientSearchResult.setRows(normalizedRows);
    clientSearchResult.setStart(normalizedStart);
    clientSearchResult.setIds(idsToRetrieve);
    clientSearchResult.setResults(results);
    clientSearchResult.setNumFound(numFound);
    clientSearchResult.setRefs(clientRelationRepresentationCreator.createRefs(type, results));
    clientSearchResult.setSortableFields(sortableFieldFinder.findFields(type));
    setNextLink(normalizedStart, normalizedRows, clientSearchResult, numFound, end, queryId);
    setPreviousLink(normalizedStart, normalizedRows, clientSearchResult, queryId);

    return clientSearchResult;
  }
}
