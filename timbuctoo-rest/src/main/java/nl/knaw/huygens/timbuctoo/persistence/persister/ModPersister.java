package nl.knaw.huygens.timbuctoo.persistence.persister;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Persister that updates the PID of the entity. For example when a Timbuctoo instance is moved to a new url the pids
 * have to point to this new url.
 */
class ModPersister implements Persister {
  private static final Logger LOG = LoggerFactory.getLogger(ModPersister.class);
  private final PersistenceWrapper persistenceWrapper;

  public ModPersister(PersistenceWrapper persistenceWrapper) {
    this.persistenceWrapper = persistenceWrapper;
  }

  @Override
  public void execute(DomainEntity domainEntity) {
    boolean shouldTry = true;
    int timesToTry = MAX_TRIES;
    while (shouldTry && timesToTry > 0)
      try {
        persistenceWrapper.updatePID(domainEntity);
        shouldTry = false;
      } catch (PersistenceException e) {
        LOG.error("Could not update pid of entity \"{}\" with id \"{}\" and rev \"{}\"", domainEntity.getClass(), domainEntity.getId(), domainEntity.getRev());
        LOG.error("Exception caught", e);
        timesToTry--;
        try {
          Thread.sleep(FIVE_SECONDS);
        } catch (InterruptedException e1) {
          LOG.warn("Could not sleep for 5 seconds.", e1);
        }
      }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
