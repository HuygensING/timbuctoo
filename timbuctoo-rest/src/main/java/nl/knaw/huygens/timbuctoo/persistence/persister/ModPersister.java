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
  private final int sleepTime;

  public ModPersister(PersistenceWrapper persistenceWrapper) {
    this(persistenceWrapper, FIVE_SECONDS);
  }

  ModPersister(PersistenceWrapper persistenceWrapper, int sleepTime) {
    this.persistenceWrapper = persistenceWrapper;
    this.sleepTime = sleepTime;
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
          Thread.sleep(sleepTime);
        } catch (InterruptedException e1) {
          LOG.warn("Sleep interrupted.", e1);
        }
      }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
