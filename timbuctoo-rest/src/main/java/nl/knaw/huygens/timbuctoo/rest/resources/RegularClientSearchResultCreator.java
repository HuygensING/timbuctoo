package nl.knaw.huygens.timbuctoo.rest.resources;

import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class RegularClientSearchResultCreator implements ClientSearchResultCreator {

  private static final Logger LOG = LoggerFactory.getLogger(RegularClientSearchResultCreator.class);
  private final Repository repository;
  private final SortableFieldFinder sortableFieldFinder;
  private final EntityRefCreator entityRefCreator;
  private final HATEOASURICreator hateoasuriCreator;

  @Inject
  public RegularClientSearchResultCreator(Repository repository, SortableFieldFinder sortableFieldFinder, EntityRefCreator entityRefCreator, HATEOASURICreator hateoasuriCreator) {
    this.repository = repository;
    this.sortableFieldFinder = sortableFieldFinder;
    this.entityRefCreator = entityRefCreator;
    this.hateoasuriCreator = hateoasuriCreator;
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

  private void setNextLink(int start, int rows, RegularClientSearchResult clientSearchResult, int numFound, int end, String queryId) {
    if (end < numFound) {
      String next = hateoasuriCreator.createHATEOASURIAsString(start + rows, rows, queryId);
      clientSearchResult.setNextLink(next);
    }
  }

  private void setPreviousLink(int start, int rows, RegularClientSearchResult clientSearchResult, String queryId) {
    if (start > 0) {
      int prevStart = Math.max(start - rows, 0);
      String prev = hateoasuriCreator.createHATEOASURIAsString(prevStart, rows, queryId);
      clientSearchResult.setPrevLink(prev.toString());
    }
  }

  /**
   * Make sure {@code value} is between {@code minValue} and {@code maxValue}.
   * @param value the value that has to be in the range
   * @param minValue the minimum value of the range
   * @param maxValue the maximum value of the range
   * @return {@code value} if it's in the range, 
   * {@code minValue} if {@code value} is lower than the {@code minValue}, 
   * {@code maxValue} if {@code value} is higher than the {@code maxValue}
   */
  private int mapToRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }

  private <T extends DomainEntity> List<T> retrieveEntities(Class<T> type, List<String> ids) {
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

}
