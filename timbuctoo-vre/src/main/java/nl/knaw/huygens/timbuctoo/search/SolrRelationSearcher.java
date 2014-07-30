package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo search
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
import nl.knaw.huygens.timbuctoo.search.converters.FacetedSearchResultConverter;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

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

  private final RelationSearchParametersConverter relationSearchParametersConverter;
  private final TypeRegistry typeRegistry;
  private final CollectionConverter collectionConverter;
  private final FacetedSearchResultConverter facetedSearchResultConverter;

  @Inject
  public SolrRelationSearcher(Repository repository, RelationSearchParametersConverter relationSearchParametersConverter, TypeRegistry typeRegistry, CollectionConverter collectionConverter,
      FacetedSearchResultConverter facetedSearchResultConverter) {
    super(repository);
    this.relationSearchParametersConverter = relationSearchParametersConverter;
    this.typeRegistry = typeRegistry;
    this.collectionConverter = collectionConverter;
    this.facetedSearchResultConverter = facetedSearchResultConverter;
  }

  @Override
  public SearchResult search(VRE vre, Class<? extends DomainEntity> relationType, RelationSearchParameters relationSearchParameters) throws SearchException, SearchValidationException {
    StopWatch getIdsStopWatch = new StopWatch();
    getIdsStopWatch.start();

    getRelationTypeIds(vre, relationSearchParameters);
    List<String> sourceSearchIds = getSearchIds(relationSearchParameters.getSourceSearchId());
    List<String> targetSearchIds = getSearchIds(relationSearchParameters.getTargetSearchId());

    getIdsStopWatch.stop();
    logStopWatchTimeInSeconds(getIdsStopWatch, "get ids");

    StopWatch convertSearchParametersStopWatch = new StopWatch();
    convertSearchParametersStopWatch.start();

    SearchParametersV1 searchParametersV1 = relationSearchParametersConverter.toSearchParametersV1(relationSearchParameters);
    searchParametersV1.getQueryOptimizer().setRows(1000000); // TODO find a better way to get all the found solr entries.

    convertSearchParametersStopWatch.stop();
    logStopWatchTimeInSeconds(convertSearchParametersStopWatch, "convert parameters");

    StopWatch getEntityStopWatch = new StopWatch();
    getEntityStopWatch.start();

    String typeString = relationSearchParameters.getTypeString();
    Class<? extends DomainEntity> type = typeRegistry.getDomainEntityType(typeString);

    getEntityStopWatch.stop();
    logStopWatchTimeInSeconds(getEntityStopWatch, "get entity");

    StopWatch getIndexStopWatch = new StopWatch();
    getIndexStopWatch.start();

    Index index = vre.getIndexForType(type);

    getIndexStopWatch.stop();
    logStopWatchTimeInSeconds(getIndexStopWatch, "get index");

    StopWatch searchStopWatch = new StopWatch();
    searchStopWatch.start();

    FacetedSearchResult facetedSearchResult = index.search(searchParametersV1);

    searchStopWatch.stop();
    logStopWatchTimeInSeconds(searchStopWatch, "search");

    StopWatch filterStopWatch = new StopWatch();
    filterStopWatch.start();

    FilterableSet<Map<String, Object>> fitlerableResults = collectionConverter.toFilterableSet(facetedSearchResult.getRawResults());
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
