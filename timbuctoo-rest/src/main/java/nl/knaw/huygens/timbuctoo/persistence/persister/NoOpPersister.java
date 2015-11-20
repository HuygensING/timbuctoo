package nl.knaw.huygens.timbuctoo.persistence.persister;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.Persister;

public class NoOpPersister implements Persister {
  @Override
  public void execute(DomainEntity domainEntity) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
