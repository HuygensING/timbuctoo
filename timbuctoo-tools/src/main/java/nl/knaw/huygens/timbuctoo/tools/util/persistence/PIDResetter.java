package nl.knaw.huygens.timbuctoo.tools.util.persistence;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.process.Progress;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Resets the persistence identifiers to point to the updated url's.
 */
public class PIDResetter {
  private Repository repository;
  private PersistenceWrapper persistenceWrapper;
  private static final Logger LOG = LoggerFactory.getLogger(PIDResetter.class);

  public static void main(String[] args) throws ConfigurationException {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config, true));

    TypeRegistry registry = injector.getInstance(TypeRegistry.class);
    Repository repository = injector.getInstance(Repository.class);
    PersistenceWrapper persistenceWrapper = injector.getInstance(PersistenceWrapper.class);

    PIDResetter resetter = new PIDResetter(repository, persistenceWrapper);

    try {
      for (Class<? extends DomainEntity> type : registry.getPrimitiveDomainEntityTypes()) {
        resetter.resetPIDsFor(type);
      }
    } finally {
      LOG.info("done");
      repository.close();
    }

  }

  public PIDResetter(Repository repository, PersistenceWrapper persistenceWrapper) {
    this.repository = repository;
    this.persistenceWrapper = persistenceWrapper;

  }

  public void resetPIDsFor(Class<? extends DomainEntity> type) {
    Progress progress = new Progress();
    LOG.info("reset PIDS for {}", type);
    for (StorageIterator<? extends DomainEntity> entities = repository.getDomainEntities(type); entities.hasNext();) {
      for (DomainEntity version : repository.getVersions(type, entities.next().getId())) {
        progress.step();
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

    progress.done();

  }

  private String getPID(DomainEntity entity) {
    if (entity.getPid() == null) {
      return null;
    }

    String[] splittedPIDURI = entity.getPid().split("/");

    return splittedPIDURI[splittedPIDURI.length - 1];
  }
}
