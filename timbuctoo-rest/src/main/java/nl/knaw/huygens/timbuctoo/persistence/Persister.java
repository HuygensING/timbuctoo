package nl.knaw.huygens.timbuctoo.persistence;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface Persister {
  void execute(DomainEntity domainEntity);
}
