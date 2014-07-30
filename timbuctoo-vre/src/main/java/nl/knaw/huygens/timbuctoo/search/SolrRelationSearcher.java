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

import java.util.List;

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.converters.RelationFacetedSearchResultConverter;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.apache.commons.lang3.time.StopWatch;

import com.google.inject.Inject;

public class SolrRelationSearcher extends RelationSearcher {

  private final RelationSearchParametersConverter relationSearchParametersConverter;
  private final TypeRegistry typeRegistry;
  private final CollectionConverter collectionConverter;

  @Inject
  public SolrRelationSearcher(Repository repository, RelationSearchParametersConverter relationSearchParametersConverter, TypeRegistry typeRegistry, CollectionConverter collectionConverter) {
    super(repository);
    this.relationSearchParametersConverter = relationSearchParametersConverter;
    this.typeRegistry = typeRegistry;
    this.collectionConverter = collectionConverter;
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

    StopWatch searchStopWatch = new StopWatch();
    searchStopWatch.start();

    SearchResult searchResult = vre.search(type, searchParametersV1, createFacetedSearchResultConverter(sourceSearchIds, targetSearchIds),
        createRelationFacetedSearchResultFilter(sourceSearchIds, targetSearchIds));

    searchStopWatch.stop();
    logStopWatchTimeInSeconds(searchStopWatch, "search");

    StopWatch filterStopWatch = new StopWatch();
    filterStopWatch.start();

    filterStopWatch.stop();
    logStopWatchTimeInSeconds(filterStopWatch, "filter");

    StopWatch convertSearchResultStopWatch = new StopWatch();
    convertSearchResultStopWatch.start();

    convertSearchResultStopWatch.stop();
    logStopWatchTimeInSeconds(convertSearchResultStopWatch, "convert search result");

    return searchResult;
  }

  protected RelationFacetedSearchResultConverter createFacetedSearchResultConverter(List<String> sourceSearchIds, List<String> targetSearchIds) {
    return new RelationFacetedSearchResultConverter(sourceSearchIds, targetSearchIds);
  }

  protected RelationFacetedSearchResultFilter createRelationFacetedSearchResultFilter(List<String> sourceIds, List<String> targetIds) {
    return new RelationFacetedSearchResultFilter(collectionConverter, sourceIds, targetIds);
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
