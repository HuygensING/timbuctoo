package nl.knaw.huygens.timbuctoo.persistence;

import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

class CollectionPersistenceRequest implements PersistenceRequest{
  private final ActionType actionType;
  private final Class<? extends DomainEntity> type;

  public CollectionPersistenceRequest(ActionType actionType, Class<? extends DomainEntity> type) {
    this.actionType = actionType;
    this.type = type;
  }

  @Override
  public Action toAction() {
    return new Action(actionType, type);
  }
}
