package nl.knaw.huygens.timbuctoo.persistence.persister;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.persistence.Persister;

public class PersisterFactory {
  private final Repository repository;
  private final PersistenceWrapper persistenceWrapper;

  public PersisterFactory(Repository repository, PersistenceWrapper persistenceWrapper) {
    this.repository = repository;
    this.persistenceWrapper = persistenceWrapper;
  }

  public Persister forActionType(ActionType actionType) {
    switch (actionType) {
      case ADD:
        return new AddPersister(repository, persistenceWrapper);
      case MOD:
        return new ModPersister(persistenceWrapper);
      default:
        return new NoOpPersister();
    }
  }
}
