package nl.knaw.huygens.timbuctoo.rest.util.search;

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
 * A search result mapper that retrieves the information from the index rather that from the database.
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
      List<Map<String, Object>> rawData = vreCollection.getVREById(searchResult.getVreId()).getRawDataFor(type, idsToRetrieve);
      LOG.debug("number of items found in index {}", rawData.size());
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
