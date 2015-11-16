package nl.knaw.huygens.timbuctoo.index.request;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class IndexRequestFactory {
  private final Repository repository;

  @Inject
  public IndexRequestFactory(Repository repository) {
    this.repository = repository;
  }

  public IndexRequest forCollectionOf(ActionType actionType, Class<? extends DomainEntity> type) {
    return new CollectionIndexRequest(actionType, type, repository);
  }

  public IndexRequest forEntity(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    return new EntityIndexRequest(actionType, type, id);
  }

  public IndexRequest forAction(Action action) {
    if(action.isForMultiEntities()) {
      return forCollectionOf(action.getActionType(), action.getType());
    }

    return forEntity(action.getActionType(), action.getType(), action.getId());
  }
}
