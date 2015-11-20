package nl.knaw.huygens.timbuctoo.persistence.request;

import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;

public class PersistenceRequestFactory {
  public PersistenceRequest forEntity(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    return new EntityPersistenceRequest(actionType, type, id);
  }

  public PersistenceRequest forCollection(ActionType actionType, Class<? extends DomainEntity> type) {
    return new CollectionPersistenceRequest(actionType, type);
  }


  public PersistenceRequest forAction(Action action) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
