package nl.knaw.huygens.timbuctoo.tools.util.persistence;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resets the persistence identifiers to point to the updated url's.
 */
public class PIDResetter {
  private Repository repository;
  private PersistenceWrapper persistenceWrapper;
  private static final Logger LOG = LoggerFactory.getLogger(PIDResetter.class);

  public static void main(String[] args) {

  }

  public PIDResetter(Repository repository, PersistenceWrapper persistenceWrapper) {
    this.repository = repository;
    this.persistenceWrapper = persistenceWrapper;

  }

  public void resetPIDsFor(Class<? extends DomainEntity> type) {
    for (StorageIterator<? extends DomainEntity> entities = repository.getDomainEntities(type); entities.hasNext();) {
      for (DomainEntity version : repository.getVersions(type, entities.next().getId())) {
        String pid = getPID(version);

        if (pid != null) {
          String id = version.getId();
          int revision = version.getRev();
          try {
            persistenceWrapper.updatePID(pid, type, id, revision);
          } catch (PersistenceException e) {
            LOG.error("PID \"{}\" of type \"{}\" with id \"{}\" and revision \"{}\"", pid, type, id, revision);
          }
        }
      }
    }

  }

  private String getPID(DomainEntity entity) {
    if (entity.getPid() == null) {
      return null;
    }

    String[] splittedPIDURI = entity.getPid().split("/");

    return splittedPIDURI[splittedPIDURI.length - 1];
  }
}
