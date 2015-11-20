package nl.knaw.huygens.timbuctoo.persistence.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import nl.knaw.huygens.timbuctoo.persistence.persister.PersisterFactory;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

class CollectionPersistenceRequest implements PersistenceRequest {
  private final Repository repository;
  private final PersisterFactory persisterFactory;
  private final ActionType actionType;
  private final Class<? extends DomainEntity> type;

  public CollectionPersistenceRequest(Repository repository, PersisterFactory persisterFactory, ActionType actionType, Class<? extends DomainEntity> type) {
    this.repository = repository;
    this.persisterFactory = persisterFactory;
    this.actionType = actionType;
    this.type = type;
  }

  @Override
  public Action toAction() {
    return new Action(actionType, type);
  }

  @Override
  public void execute() {
    Persister persister = persisterFactory.forActionType(actionType);
    for (StorageIterator<? extends DomainEntity> it = repository.getDomainEntities(type); it.hasNext(); ) {
      persister.execute(it.next());
    }
  }
}
