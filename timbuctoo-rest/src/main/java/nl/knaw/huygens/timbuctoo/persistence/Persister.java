package nl.knaw.huygens.timbuctoo.persistence;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface Persister {
  int FIVE_SECONDS = 5000;
  int MAX_TRIES = 5;

  void execute(DomainEntity domainEntity);
}
