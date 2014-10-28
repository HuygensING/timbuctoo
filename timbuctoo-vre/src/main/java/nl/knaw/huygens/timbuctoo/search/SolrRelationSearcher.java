package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo VRE
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
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

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
  public SearchResult search(VRE vre, Class<? extends DomainEntity> relationType, RelationSearchParameters parameters) throws SearchException, SearchValidationException {
    addRelationTypeIds(vre, parameters);

    SearchResult sourceResult = repository.getEntity(SearchResult.class, parameters.getSourceSearchId());
    String sourceType = sourceResult.getSearchType();
    List<String> sourceSearchIds = sourceResult.getIds();

    SearchResult targetResult = repository.getEntity(SearchResult.class, parameters.getTargetSearchId());
    String targetType = targetResult.getSearchType();
    List<String> targetSearchIds = targetResult.getIds();

    List<String> relationTypeIds = parameters.getRelationTypeIds();

    SearchParametersV1 parametersV1 = relationSearchParametersConverter.toSearchParametersV1(parameters);
    parametersV1.getQueryOptimizer().setRows(1000000); // TODO find a better way to get all the found solr entries.

    String typeString = parameters.getTypeString();
    Class<? extends DomainEntity> type = typeRegistry.getDomainEntityType(typeString);

    SearchResult searchResult = vre.search(type, parametersV1, createRelationFacetedSearchResultFilter(sourceSearchIds, targetSearchIds));

    searchResult.setVreId(vre.getVreId());
    searchResult.setRelationSearch(true);
    searchResult.setSourceType(sourceType);
    searchResult.setSourceIds(sourceSearchIds);
    searchResult.setTargetType(targetType);
    searchResult.setTargetIds(targetSearchIds);
    searchResult.setRelationTypeIds(relationTypeIds);

    return searchResult;
  }

  protected RelationFacetedSearchResultFilter createRelationFacetedSearchResultFilter(List<String> sourceIds, List<String> targetIds) {
    return new RelationFacetedSearchResultFilter(collectionConverter, sourceIds, targetIds);
  }

  private void addRelationTypeIds(VRE vre, RelationSearchParameters parameters) {
    if (parameters.getRelationTypeIds() == null || parameters.getRelationTypeIds().isEmpty()) {
      parameters.setRelationTypeIds(repository.getRelationTypeIdsByName(vre.getReceptionNames()));
    }
  }

}
