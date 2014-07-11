package nl.knaw.huygens.timbuctoo.rest.resources;

import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;

public class RelationClientSearchResultCreator extends ClientSearchResultCreator {

  private final ClientRelationRepresentationCreator clientRelationRepresentationCreator;

  public RelationClientSearchResultCreator(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator,
      ClientRelationRepresentationCreator clientRelationRepresentationCreator) {
    super(repository, sortableFieldFinder, hateoasURICreator);
    this.clientRelationRepresentationCreator = clientRelationRepresentationCreator;
  }

  @Override
  public <T extends DomainEntity> RelationClientSearchResult create(Class<T> type, SearchResult searchResult, int start, int rows) {
    RelationClientSearchResult clientSearchResult = new RelationClientSearchResult();

    String queryId = searchResult.getId();
    List<String> ids = searchResult.getIds();
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
