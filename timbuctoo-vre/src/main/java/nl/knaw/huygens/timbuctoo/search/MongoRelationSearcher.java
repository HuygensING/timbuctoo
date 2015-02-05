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
import java.util.Set;

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Singleton;

// TODO: When this class will be removed remove the 'getRelationsByType'-method in Repository as well.
/**
 * 
 * @deprecated use SolrRelationSearcher instead
 */
@Singleton
@Deprecated
public class MongoRelationSearcher extends RelationSearcher {

  private final CollectionConverter collectionConverter;
  private final RelationSearchResultCreator searchResultCreator;

  @Inject
  public MongoRelationSearcher(Repository repository, CollectionConverter collectionConverter, RelationSearchResultCreator searchResultCreator) {
    super(repository);
    this.collectionConverter = collectionConverter;
    this.searchResultCreator = searchResultCreator;
  }

  @Override
  public SearchResult search(VRE vre, Class<? extends DomainEntity> relationType, RelationSearchParameters parameters) throws SearchException {

    SearchResult sourceResult = repository.getEntityOrDefaultVariation(SearchResult.class, parameters.getSourceSearchId());
    String sourceType = sourceResult.getSearchType();
    List<String> sourceIds = sourceResult.getIds();

    SearchResult targetResult = repository.getEntityOrDefaultVariation(SearchResult.class, parameters.getTargetSearchId());
    String targetType = targetResult.getSearchType();
    List<String> targetIds = targetResult.getIds();

    List<String> relationTypeIds = getRelationTypes(parameters.getRelationTypeIds(), vre);

    // Retrieve the relations
    FilterableSet<Relation> relations = getRelationsAsFilterableSet(relationTypeIds);

    // Start filtering
    Predicate<Relation> predicate = new RelationSourceTargetPredicate<Relation>(sourceIds, targetIds);
    Set<Relation> filteredRelations = relations.filter(predicate);

    // Create the search result
    String typeString = parameters.getTypeString();
    return searchResultCreator.create(vre.getVreId(), typeString, filteredRelations, sourceType, sourceIds, targetType, targetIds, relationTypeIds);
  }

  private FilterableSet<Relation> getRelationsAsFilterableSet(List<String> relationTypeIds) throws SearchException {
    List<Relation> relations;
    try {
      relations = repository.getRelationsByType(Relation.class, relationTypeIds);
    } catch (StorageException e) {
      throw new SearchException(e);
    }
    return collectionConverter.toFilterableSet(relations);
  }

}
