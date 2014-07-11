package nl.knaw.huygens.timbuctoo.rest.resources;

import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.ClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
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

  /**
   * Make sure {@code value} is between {@code minValue} and {@code maxValue}.
   * @param value the value that has to be in the range
   * @param minValue the minimum value of the range
   * @param maxValue the maximum value of the range
   * @return {@code value} if it's in the range, 
   * {@code minValue} if {@code value} is lower than the {@code minValue}, 
   * {@code maxValue} if {@code value} is higher than the {@code maxValue}
   */
  protected int mapToRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }

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

}