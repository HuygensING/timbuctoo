package nl.knaw.huygens.timbuctoo.search;

/*
 * #%L
 * Timbuctoo VRE
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import com.google.inject.Inject;
import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.search.converters.RelationSearchParametersConverter;
import nl.knaw.huygens.timbuctoo.vre.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.SearchValidationException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREException;

import java.util.List;

public class IndexRelationSearcher extends RelationSearcher {

  private final RelationSearchParametersConverter relationSearchParametersConverter;
  private final TypeRegistry typeRegistry;
  private final CollectionConverter collectionConverter;

  @Inject
  public IndexRelationSearcher(Repository repository, RelationSearchParametersConverter relationSearchParametersConverter, TypeRegistry typeRegistry, CollectionConverter collectionConverter) {
    super(repository);
    this.relationSearchParametersConverter = relationSearchParametersConverter;
    this.typeRegistry = typeRegistry;
    this.collectionConverter = collectionConverter;
  }

  @Override
  public SearchResult search(VRE vre, Class<? extends DomainEntity> relationType, RelationSearchParameters parameters) throws SearchException, SearchValidationException {
    SearchResult sourceResult = repository.getEntityOrDefaultVariation(SearchResult.class, parameters.getSourceSearchId());
    String sourceType = sourceResult.getSearchType();
    List<String> sourceSearchIds = sourceResult.getIds();

    SearchResult targetResult = repository.getEntityOrDefaultVariation(SearchResult.class, parameters.getTargetSearchId());
    String targetType = targetResult.getSearchType();
    List<String> targetSearchIds = targetResult.getIds();

    List<String> relationTypeIds = null;

      relationTypeIds = getRelationTypeId(vre, parameters, sourceType, targetType);


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
    searchResult.setFacets(targetResult.getFacets());
    searchResult.setTerm(targetResult.getTerm());

    return searchResult;
  }

  private List<String> getRelationTypeId(VRE vre, RelationSearchParameters parameters, String sourceTypeName, String targetTypeName
  ) throws SearchException {

    if(parameters.getRelationTypeIds() == null || parameters.getRelationTypeIds().isEmpty()){
      try {
        Class<? extends DomainEntity> sourceType = typeRegistry.getDomainEntityType(sourceTypeName);
        Class<? extends DomainEntity> targetType = typeRegistry.getDomainEntityType(targetTypeName);

        return repository.getRelationTypeIdsByName(vre.getRelationTypeNamesBetween(sourceType, targetType));
      }
      catch (VREException e){
        throw new SearchException(e);
      }
    }

    return repository.getRelationTypeIdsByName(parameters.getRelationTypeIds());
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
