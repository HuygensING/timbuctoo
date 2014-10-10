package nl.knaw.huygens.timbuctoo.tools.util.persistence;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

/**
 * Resets the persistence identifiers to point to the updated url's.
 */
public class PIDResetter {
  private Repository repository;
  private PersistenceWrapper persistenceWrapper;

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
          persistenceWrapper.updatePID(pid, type, version.getId(), version.getRev());
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
