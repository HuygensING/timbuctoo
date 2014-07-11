package nl.knaw.huygens.timbuctoo.rest.resources;

import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;

import com.google.inject.Inject;

public class RegularClientSearchResultCreator extends ClientSearchResultCreator {

  private final ClientEntityRepresentationCreator entityRefCreator;

  @Inject
  public RegularClientSearchResultCreator(Repository repository, SortableFieldFinder sortableFieldFinder, ClientEntityRepresentationCreator entityRefCreator, HATEOASURICreator hateoasURICreator) {
    super(repository, sortableFieldFinder, hateoasURICreator);
    this.entityRefCreator = entityRefCreator;
  }

  @Override
  public <T extends DomainEntity> RegularClientSearchResult create(Class<T> type, SearchResult searchResult, int start, int rows) {
    RegularClientSearchResult clientSearchResult = new RegularClientSearchResult();

    List<String> ids = searchResult.getIds();
    int numFound = ids.size();
    int normalizedStart = mapToRange(start, 0, numFound);
    int normalizedRows = mapToRange(rows, 0, numFound - start);
    int end = normalizedStart + normalizedRows;

    List<String> idsToRetrieve = ids.subList(normalizedStart, end);
    List<T> results = retrieveEntities(type, idsToRetrieve);

    String queryId = searchResult.getId();

    clientSearchResult.setRows(normalizedRows);
    clientSearchResult.setStart(normalizedStart);
    clientSearchResult.setIds(idsToRetrieve);
    clientSearchResult.setResults(results);
    clientSearchResult.setNumFound(numFound);
    clientSearchResult.setRefs(entityRefCreator.createRefs(type, results));
    clientSearchResult.setSortableFields(sortableFieldFinder.findFields(type));
    clientSearchResult.setTerm(searchResult.getTerm());
    clientSearchResult.setFacets(searchResult.getFacets());

    setPreviousLink(start, rows, clientSearchResult, queryId);

    setNextLink(start, rows, clientSearchResult, numFound, end, queryId);

    return clientSearchResult;
  }

}
