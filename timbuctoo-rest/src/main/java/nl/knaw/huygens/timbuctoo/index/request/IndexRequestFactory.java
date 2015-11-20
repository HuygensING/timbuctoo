package nl.knaw.huygens.timbuctoo.index.request;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;

public class IndexRequestFactory {
  private final IndexerFactory indexerFactory;
  private final Repository repository;

  @Inject
  public IndexRequestFactory(IndexerFactory indexerFactory, Repository repository) {
    this.indexerFactory = indexerFactory;
    this.repository = repository;
  }

  public IndexRequest forCollectionOf(ActionType actionType, Class<? extends DomainEntity> type) {
    return new CollectionIndexRequest(indexerFactory, actionType, type, repository);
  }

  public IndexRequest forEntity(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    if(Relation.class.isAssignableFrom(type)){
      return new RelationIndexRequest(indexerFactory, actionType, type, id);
    }
    return new EntityIndexRequest(indexerFactory, actionType, type, id);
  }

  public IndexRequest forAction(Action action) {
    if(action.isForMultiEntities()) {
      return forCollectionOf(action.getActionType(), action.getType());
    }

    return forEntity(action.getActionType(), action.getType(), action.getId());
  }
}
