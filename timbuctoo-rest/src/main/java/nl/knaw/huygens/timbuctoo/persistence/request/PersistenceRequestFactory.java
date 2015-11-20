package nl.knaw.huygens.timbuctoo.persistence.request;

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.PersisterFactory;

public class PersistenceRequestFactory {

  private Repository repository;
  private PersisterFactory persisterFactory;

  @Inject
  public PersistenceRequestFactory(Repository repository, PersisterFactory persisterFactory){
    this.repository = repository;
    this.persisterFactory = persisterFactory;
  }

  public PersistenceRequest forEntity(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    return new EntityPersistenceRequest(actionType, type, id);
  }

  public PersistenceRequest forCollection(ActionType actionType, Class<? extends DomainEntity> type) {
    return new CollectionPersistenceRequest(repository, persisterFactory, actionType, type);
  }

  public PersistenceRequest forAction(Action action) {
    if (action.isForMultiEntities()) {
      return forCollection(action.getActionType(), action.getType());
    }
    return forEntity(action.getActionType(), action.getType(), action.getId());
  }
}
