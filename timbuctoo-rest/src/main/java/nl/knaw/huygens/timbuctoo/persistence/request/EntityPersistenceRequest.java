package nl.knaw.huygens.timbuctoo.persistence.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.persister.PersisterFactory;

class EntityPersistenceRequest implements PersistenceRequest {
  private final ActionType actionType;
  private final Class<? extends DomainEntity> type;
  private final String id;

  public EntityPersistenceRequest(Repository repository, PersisterFactory persisterFactory, ActionType actionType, Class<? extends DomainEntity> type, String id) {
    this.actionType = actionType;
    this.type = type;
    this.id = id;
  }

  @Override
  public Action toAction() {
    return new Action(actionType, type, id);
  }

  @Override
  public void execute() {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
