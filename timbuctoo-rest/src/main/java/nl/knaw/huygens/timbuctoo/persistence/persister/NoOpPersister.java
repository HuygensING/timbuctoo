package nl.knaw.huygens.timbuctoo.persistence.persister;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NoOpPersister implements Persister {
  Logger LOG = LoggerFactory.getLogger(NoOpPersister.class);

  @Override
  public void execute(DomainEntity domainEntity) {
    LOG.info("Doing nothing for DomainEntity of type \"{}\" with id \"{}\"", domainEntity.getClass(), domainEntity.getId());
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
