package nl.knaw.huygens.timbuctoo.search;

import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_ID_FACET_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_ID_FACET_NAME;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;
import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.apache.commons.lang3.time.StopWatch;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class SolrRelationSearcher extends RelationSearcher {

  private static final class TargetSourceIdRelationPredicate implements Predicate<Map<String, Object>> {
    private final Set<String> targetSearchIds;
    private final Set<String> sourceSearchIds;

    private TargetSourceIdRelationPredicate(List<String> targetSearchIds, List<String> sourceSearchIds) {
      this.targetSearchIds = Sets.newTreeSet(targetSearchIds);
      this.sourceSearchIds = Sets.newTreeSet(sourceSearchIds);
    }

    @Override
    public boolean apply(Map<String, Object> input) {

      return sourceSearchIds.contains(getSourceIds(input)) && targetSearchIds.contains(getTargetIds(input));
    }

    private String getTargetIds(Map<String, Object> input) {
      return getFirstValueAsString(input, TARGET_ID_FACET_NAME);
    }

    @SuppressWarnings("unchecked")
    private String getFirstValueAsString(Map<String, Object> input, String fieldName) {
      return ((List<String>) input.get(fieldName)).get(0);
    }

    private String getSourceIds(Map<String, Object> input) {
      return getFirstValueAsString(input, SOURCE_ID_FACET_NAME);
    }
  }

  private final VREManager vreManager;
  private final RelationSearchParametersConverter relationSearchParametersConverter;
  private final TypeRegistry typeRegistry;
  private final CollectionConverter collectionConverter;
  private final FacetedSearchResultConverter facetedSearchResultConverter;

  @Inject
  public SolrRelationSearcher(Repository repository, VREManager vreManager, RelationSearchParametersConverter relationSearchParametersConverter, TypeRegistry typeRegistry,
      CollectionConverter collectionConverter, FacetedSearchResultConverter facetedSearchResultConverter) {
    super(repository);
    this.vreManager = vreManager;
    this.relationSearchParametersConverter = relationSearchParametersConverter;
    this.typeRegistry = typeRegistry;
    this.collectionConverter = collectionConverter;
    this.facetedSearchResultConverter = facetedSearchResultConverter;
  }

  @Override
  public SearchResult search(VRE vre, RelationSearchParameters relationSearchParameters) throws SearchException, SearchValidationException {
    StopWatch getIdsStopWatch = new StopWatch();
    getIdsStopWatch.start();
    getRelationTypeIds(vre, relationSearchParameters);
    final List<String> sourceSearchIds = getSearchIds(relationSearchParameters.getSourceSearchId());
    final List<String> targetSearchIds = getSearchIds(relationSearchParameters.getTargetSearchId());
    getIdsStopWatch.stop();
    logStopWatchTimeInSeconds(getIdsStopWatch, "get ids");

    StopWatch convertSearchParametersStopWatch = new StopWatch();
    convertSearchParametersStopWatch.start();
    SearchParametersV1 searchParametersV1 = relationSearchParametersConverter.toSearchParamtersV1(relationSearchParameters);
    searchParametersV1.getQueryOptimizer().setRows(1000000);
    convertSearchParametersStopWatch.stop();
    logStopWatchTimeInSeconds(convertSearchParametersStopWatch, "convert parameters");

    StopWatch getEntityStopWatch = new StopWatch();
    getEntityStopWatch.start();
    final String typeString = relationSearchParameters.getTypeString();
    Class<? extends DomainEntity> type = typeRegistry.getDomainEntityType(typeString);
    getEntityStopWatch.stop();
    logStopWatchTimeInSeconds(getEntityStopWatch, "get entity");

    StopWatch getIndexStopWatch = new StopWatch();
    getIndexStopWatch.start();
    Index index = vreManager.getIndexFor(vre, type);
    getIndexStopWatch.stop();
    logStopWatchTimeInSeconds(getIndexStopWatch, "get index");

    StopWatch searchStopWatch = new StopWatch();
    searchStopWatch.start();
    FacetedSearchResult facetedSearchResult = index.search(searchParametersV1);
    searchStopWatch.stop();
    logStopWatchTimeInSeconds(searchStopWatch, "search");

    StopWatch filterStopWatch = new StopWatch();
    filterStopWatch.start();
    StopWatch convertListToFilterableSetStopWatch = new StopWatch();
    convertListToFilterableSetStopWatch.start();
    FilterableSet<Map<String, Object>> fitlerableResults = collectionConverter.toFilterableSet(facetedSearchResult.getRawResults());
    convertListToFilterableSetStopWatch.stop();
    logStopWatchTimeInSeconds(convertListToFilterableSetStopWatch, "convert raw result");
    Set<Map<String, Object>> filteredSet = fitlerableResults.filter(new TargetSourceIdRelationPredicate(targetSearchIds, sourceSearchIds));
    facetedSearchResult.setRawResults(Lists.newArrayList(filteredSet));
    filterStopWatch.stop();
    logStopWatchTimeInSeconds(filterStopWatch, "filter");

    StopWatch convertSearchResultStopWatch = new StopWatch();
    convertSearchResultStopWatch.start();
    SearchResult searchResult = facetedSearchResultConverter.convert(relationSearchParameters.getTypeString(), facetedSearchResult);
    searchResult.setSourceIds(sourceSearchIds);
    searchResult.setTargetIds(targetSearchIds);
    searchResult.setRelationSearch(true);
    convertSearchResultStopWatch.stop();
    logStopWatchTimeInSeconds(convertSearchResultStopWatch, "convert search result");

    return searchResult;
  }

  private List<String> getSearchIds(String id) {
    SearchResult result = repository.getEntity(SearchResult.class, id);

    return result.getIds();
  }

  private void getRelationTypeIds(VRE vre, RelationSearchParameters relationSearchParameters) {
    if (relationSearchParameters.getRelationTypeIds() == null || relationSearchParameters.getRelationTypeIds().isEmpty()) {
      relationSearchParameters.setRelationTypeIds(repository.getRelationTypeIdsByName(vre.getReceptionNames()));
    }
  }
}
