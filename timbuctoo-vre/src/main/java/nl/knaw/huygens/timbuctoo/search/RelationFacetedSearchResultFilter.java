package nl.knaw.huygens.timbuctoo.search;

import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_ID_FACET_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_ID_FACET_NAME;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.FacetedSearchResult;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class RelationFacetedSearchResultFilter implements FacetedSearchResultProcessor {

  private final CollectionConverter collectionConverter;
  private TargetSourceIdRelationPredicate predicate;

  public RelationFacetedSearchResultFilter(CollectionConverter collectionConverter, List<String> sourceSearchIds, List<String> targetSearchIds) {
    this.collectionConverter = collectionConverter;
    predicate = new TargetSourceIdRelationPredicate(targetSearchIds, sourceSearchIds);
  }

  @Override
  public FacetedSearchResult process(FacetedSearchResult facetedSearchResult) {
    FilterableSet<Map<String, Object>> filterableRawResults = collectionConverter.toFilterableSet(facetedSearchResult.getRawResults());

    facetedSearchResult.setRawResults(Lists.newArrayList(filterableRawResults.filter(predicate)));

    return facetedSearchResult;
  }

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

  protected FacetedSearchResult createProcessedFacetedSearchResult() {
    return new FacetedSearchResult();
  }

}
