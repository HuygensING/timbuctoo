package nl.knaw.huygens.timbuctoo.persistence.persister;

import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.persistence.Persister;

public class PersisterFactory {
  public Persister forActionType(ActionType actionType) {
    switch (actionType) {
      case ADD:
        return new AddPersister();
      case MOD:
        return new ModPersister();
      default:
        return new NoOpPersister();
    }
  }
}
