package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RegularSearchResultDTO;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.util.HATEOASURICreator;
import nl.knaw.huygens.timbuctoo.rest.util.RangeHelper;
import nl.knaw.huygens.timbuctoo.search.FullTextSearchFieldFinder;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRECollection;

import java.util.List;
import java.util.Map;

/**
 * A search result mapper that retrieves the information from the index rather that from the database.
 */
public class IndexRegularSearchResultMapper extends RegularSearchResultMapper {
  private DomainEntityDTOFactory domainEntityDTOFactory;

  @Inject
  public IndexRegularSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, FullTextSearchFieldFinder fullTextSearchFieldFinder, VRECollection vreCollection) {
    this(repository, sortableFieldFinder, hateoasURICreator, fullTextSearchFieldFinder, vreCollection, new RangeHelper(), new DomainEntityDTOFactory());
  }

  IndexRegularSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator hateoasURICreator, FullTextSearchFieldFinder fullTextSearchFieldFinder, VRECollection vreCollection, RangeHelper rangeHelper) {
    super(repository, sortableFieldFinder, hateoasURICreator, fullTextSearchFieldFinder, vreCollection, rangeHelper);
  }

  public IndexRegularSearchResultMapper(Repository repository, SortableFieldFinder sortableFieldFinder, HATEOASURICreator uriCreator, FullTextSearchFieldFinder fullTextSearchFieldFinder, VRECollection vreCollection, RangeHelper rangeHelper, DomainEntityDTOFactory domainEntityDTOFactory) {
    super(repository, sortableFieldFinder, uriCreator, fullTextSearchFieldFinder, vreCollection, rangeHelper);
    this.domainEntityDTOFactory = domainEntityDTOFactory;
  }

  @Override
  public <T extends DomainEntity> RegularSearchResultDTO create(Class<T> type, SearchResult searchResult, int start, int rows) {
    List<String> ids = getIds(searchResult);
    int numFound = ids.size();
    int normalizedStart = mapToRange(start, 0, numFound);
    int normalizedRows = mapToRange(rows, 0, numFound - normalizedStart);
    int end = normalizedStart + normalizedRows;

    List<String> idsToRetrieve = ids.subList(normalizedStart, end);

    List<Map<String, Object>> rawData = null;
    try {
      rawData = vreCollection.getVREById(searchResult.getVreId()).getRawDataFor(type, idsToRetrieve);
    } catch (SearchException | NotInScopeException e) {
      throw new RuntimeException(e); // FIXME: Hack to inform the client the search went wrong, and not change the API
    }

    String queryId = searchResult.getId();

    RegularSearchResultDTO dto = new RegularSearchResultDTO();

    dto.setRows(normalizedRows);
    dto.setStart(normalizedStart);
    dto.setIds(ids);
    dto.setNumFound(numFound);
    dto.setRefs(domainEntityDTOFactory.createFor(rawData));
    dto.setSortableFields(sortableFieldFinder.findFields(type));
    dto.setTerm(searchResult.getTerm());
    dto.setFacets(searchResult.getFacets());
    dto.setFullTextSearchFields(fullTextSearchFieldFinder.findFields(type));

    dto.setNextLink(hateoasURICreator.createNextResultsAsString(normalizedStart, normalizedRows, numFound, queryId));
    dto.setPrevLink(hateoasURICreator.createPrevResultsAsString(normalizedStart, normalizedRows, queryId));

    return dto;
  }
}
