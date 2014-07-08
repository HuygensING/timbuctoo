package nl.knaw.huygens.timbuctoo.search;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MongoRelationSearcher extends RelationSearcher {

  private final CollectionConverter collectionConverter;
  private final RelationSearchResultCreator relationSearchResultCreator;
  static final Logger LOG = LoggerFactory.getLogger(MongoRelationSearcher.class);

  @Inject
  public MongoRelationSearcher(Repository repository, CollectionConverter collectionConverter, RelationSearchResultCreator relationSearchResultCreator) {
    super(repository);
    this.collectionConverter = collectionConverter;
    this.relationSearchResultCreator = relationSearchResultCreator;
  }

  @Override
  public SearchResult search(VRE vre, Class<? extends DomainEntity> relationType, RelationSearchParameters relationSearchParameters) throws SearchException {
    List<String> sourceIds = getSearchResultIds(relationSearchParameters.getSourceSearchId());
    List<String> targetIds = getSearchResultIds(relationSearchParameters.getTargetSearchId());

    List<String> relationTypeIds = getRelationTypes(relationSearchParameters.getRelationTypeIds(), vre);

    // retrieve the relations
    StopWatch relationRetrievelStopWatch = new StopWatch();
    relationRetrievelStopWatch.start();

    FilterableSet<Relation> filterableRelations = getRelationsAsFilterableSet(relationTypeIds);

    relationRetrievelStopWatch.stop();
    logStopWatchTimeInSeconds(relationRetrievelStopWatch, "relation retrieval duration");

    //Start filtering
    StopWatch filterStopWatch = new StopWatch();
    filterStopWatch.start();

    Predicate<Relation> predicate = new RelationSourceTargetPredicate<Relation>(sourceIds, targetIds);
    Set<Relation> filteredRelations = filterableRelations.filter(predicate);

    filterStopWatch.stop();
    logStopWatchTimeInSeconds(filterStopWatch, "filter duration");

    //Create the search result
    StopWatch searchResultCreationStopWatch = new StopWatch();
    searchResultCreationStopWatch.start();

    SearchResult searchResult = relationSearchResultCreator.create(filteredRelations, sourceIds, targetIds, relationTypeIds, relationSearchParameters.getTypeString());

    searchResultCreationStopWatch.stop();
    logStopWatchTimeInSeconds(searchResultCreationStopWatch, "search result creation");

    return searchResult;
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

  private List<String> getSearchResultIds(String searchId) {
    SearchResult sourceSearch = repository.getEntity(SearchResult.class, searchId);
    return sourceSearch.getIds();
  }

}
