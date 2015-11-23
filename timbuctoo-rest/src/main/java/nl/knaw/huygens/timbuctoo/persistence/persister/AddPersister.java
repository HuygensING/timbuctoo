package nl.knaw.huygens.timbuctoo.persistence.persister;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.AlreadyHasAPidException;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AddPersister implements Persister {
  public static final String DOMAIN_ENTITY_CANNOT_BE_NULL_MESSAGE = "DomainEntity to persist cannot be null.";
  public static final Logger LOG = LoggerFactory.getLogger(AddPersister.class);
  private final Repository repository;
  private final PersistenceWrapper persistenceWrapper;
  private final int sleepTime;

  public AddPersister(Repository repository, PersistenceWrapper persistenceWrapper) {
    this(repository, persistenceWrapper, FIVE_SECONDS);
  }

  AddPersister(Repository repository, PersistenceWrapper persistenceWrapper, int sleepTime){
    this.repository = repository;
    this.persistenceWrapper = persistenceWrapper;
    this.sleepTime = sleepTime;
  }

  @Override
  public void execute(DomainEntity domainEntity) {
    if (domainEntity == null) {
      throw new IllegalArgumentException(DOMAIN_ENTITY_CANNOT_BE_NULL_MESSAGE);
    }

    Class<? extends DomainEntity> type = domainEntity.getClass();
    String id = domainEntity.getId();
    String pid = null;

    pid = persistEntity(domainEntity, type, id);

    if (pid != null) {
      int timesToTry = MAX_TRIES;
      boolean shouldTry = true;
      while (shouldTry && timesToTry > 0) {
        try {
          repository.setPID(type, id, pid);
          shouldTry = false;
        } catch (AlreadyHasAPidException e) {
          deletePID(pid);
          shouldTry = false;
        } catch (Exception e) {
          LOG.error("Could not set pid \"{}\" to entity \"{}\" with id \"{}\".", pid, type, id);
          LOG.error("Exception caught", e);
          timesToTry--;
          if (timesToTry == 0) {
            deletePID(pid);
          }
          try {
            Thread.sleep(sleepTime);
          } catch (InterruptedException e1) {
            LOG.warn("Sleep interrupted.", e1);
          }
        }
      }
    }
  }

  private void deletePID(String pid) {
    try {
      persistenceWrapper.deletePersistentId(pid);
    } catch (PersistenceException e1) {
      LOG.info("Could not delete PID", e1);
    }
  }

  private String persistEntity(DomainEntity domainEntity, Class<? extends DomainEntity> type, String id) {
    int timesToTry = MAX_TRIES;
    String pid = null;
    while (pid == null && timesToTry > 0) {
      try {
        pid = persistenceWrapper.persistObject(type, id, domainEntity.getRev());
      } catch (PersistenceException e) {
        LOG.error("Could not persist entity \"{}\" with id \"{}\".", type, id);
        LOG.error("Exception caught", e);
        timesToTry--;
        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException e1) {
          LOG.warn("Sleep interrupted.", e1);
        }
      }
    }
    return pid;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
