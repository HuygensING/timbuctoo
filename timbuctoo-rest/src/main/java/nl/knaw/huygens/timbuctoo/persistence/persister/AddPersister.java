package nl.knaw.huygens.timbuctoo.persistence.persister;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AddPersister implements Persister {
  public static final String DOMAIN_ENTITY_CANNOT_BE_NULL_MESSAGE = "DomainEntity to persist cannot be null.";
  public static final Logger LOG = LoggerFactory.getLogger(AddPersister.class);
  private final Repository repository;
  private final PersistenceWrapper persistenceWrapper;

  public AddPersister(Repository repository, PersistenceWrapper persistenceWrapper) {
    this.repository = repository;
    this.persistenceWrapper = persistenceWrapper;
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
        } catch (StorageException e) {
          LOG.error("Could not set pid \"{}\" to entity \"{}\" with id \"{}\".", pid, type, id);
          LOG.error("Exception caught", e);
          timesToTry--;
          if(timesToTry == 0){
            deletePID(pid);
          }
          try {
            Thread.sleep(FIVE_SECONDS);
          } catch (InterruptedException e1) {
            LOG.warn("Could not sleep for 5 seconds.", e1);
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
    int timesToTry = 5;
    String pid = null;
    while (pid == null && timesToTry > 0) {
      try {
        pid = persistenceWrapper.persistObject(type, id, domainEntity.getRev());
      } catch (PersistenceException e) {
        LOG.error("Could not persist entity \"{}\" with id \"{}\".", type, id);
        LOG.error("Exception caught", e);
        timesToTry--;
        try {
          Thread.sleep(FIVE_SECONDS);
        } catch (InterruptedException e1) {
          LOG.warn("Could not sleep for 5 seconds.", e1);
        }
      }
    }
    return pid;
  }
}
