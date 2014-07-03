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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class SolrRelationSearcher extends RelationSearcher {

  private static final class TargetSourceIdRelationPredicate implements Predicate<Map<String, Object>> {
    private final List<String> targetSearchIds;
    private final List<String> sourceSearchIds;

    private TargetSourceIdRelationPredicate(List<String> targetSearchIds, List<String> sourceSearchIds) {
      this.targetSearchIds = targetSearchIds;
      this.sourceSearchIds = sourceSearchIds;
    }

    @Override
    public boolean apply(Map<String, Object> input) {
      @SuppressWarnings("unchecked")
      List<String> sourceIds = (List<String>) input.get(SOURCE_ID_FACET_NAME);
      @SuppressWarnings("unchecked")
      List<String> targetIds = (List<String>) input.get(TARGET_ID_FACET_NAME);

      return sourceSearchIds.containsAll(sourceIds) && targetSearchIds.containsAll(targetIds);
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
    getRelationTypeIds(vre, relationSearchParameters);

    final List<String> sourceSearchIds = getSearchIds(relationSearchParameters.getSourceSearchId());
    final List<String> targetSearchIds = getSearchIds(relationSearchParameters.getTargetSearchId());

    SearchParametersV1 searchParametersV1 = relationSearchParametersConverter.toSearchParamtersV1(relationSearchParameters);

    final String typeString = relationSearchParameters.getTypeString();
    Class<? extends DomainEntity> type = typeRegistry.getDomainEntityType(typeString);

    Index index = vreManager.getIndexFor(vre, type);

    FacetedSearchResult facetedSearchResult = index.search(searchParametersV1);

    FilterableSet<Map<String, Object>> fitlerableResults = collectionConverter.toFilterableSet(facetedSearchResult.getRawResults());
    Set<Map<String, Object>> filteredSet = fitlerableResults.filter(new TargetSourceIdRelationPredicate(targetSearchIds, sourceSearchIds));

    facetedSearchResult.setRawResults(Lists.newArrayList(filteredSet));

    SearchResult searchResult = facetedSearchResultConverter.convert(relationSearchParameters.getTypeString(), facetedSearchResult);
    searchResult.setSourceIds(sourceSearchIds);
    searchResult.setTargetIds(targetSearchIds);
    searchResult.setRelationSearch(true);

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
