package nl.knaw.huygens.timbuctoo.search;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.SearchException;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import com.google.common.base.Predicate;

public class RelationSearcher {

  private final Repository repository;
  private final CollectionConverter collectionConverter;
  private final RelationSearchResultCreator relationSearchResultCreator;

  public RelationSearcher(Repository repository, CollectionConverter collectionConverter, RelationSearchResultCreator relationSearchResultCreator) {
    this.repository = repository;
    this.collectionConverter = collectionConverter;
    this.relationSearchResultCreator = relationSearchResultCreator;
  }

  public SearchResult search(VRE vre, RelationSearchParameters relationSearchParameters) throws SearchException {
    List<String> sourceIds = getSearchResultIds(relationSearchParameters.getSourceSearchId());
    List<String> targetIds = getSearchResultIds(relationSearchParameters.getTargetSearchId());

    List<String> relationTypeIds = getRelationTypes(relationSearchParameters.getRelationTypeIds(), vre);

    FilterableSet<Relation> filterableRelations = getRelationsAsFilterableSet(relationTypeIds);

    Predicate<Relation> predicate = new RelationSourceTargetPredicate<Relation>(sourceIds, targetIds);
    Set<Relation> filteredRelations = filterableRelations.filter(predicate);

    return relationSearchResultCreator.create(filteredRelations, sourceIds, targetIds, relationTypeIds, relationSearchParameters.getTypeString());
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

  private List<String> getRelationTypes(List<String> relationTypeIds, VRE vre) {
    if (relationTypeIds != null && !relationTypeIds.isEmpty()) {
      return relationTypeIds;
    }

    // TODO find a more generic way, to retrieve the relation ids of a VRE.
    return repository.getRelationTypeIdsByName(vre.getReceptionNames());
  }

  private List<String> getSearchResultIds(String searchId) {
    SearchResult sourceSearch = repository.getEntity(SearchResult.class, searchId);
    return sourceSearch.getIds();
  }

}
