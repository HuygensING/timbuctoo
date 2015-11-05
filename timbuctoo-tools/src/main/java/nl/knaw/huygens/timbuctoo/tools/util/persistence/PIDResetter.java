package nl.knaw.huygens.timbuctoo.tools.util.persistence;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.inject.Injector;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.process.Progress;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resets the persistence identifiers to point to the updated url's.
 */
public class PIDResetter {
  private Repository repository;
  private PersistenceWrapper persistenceWrapper;
  private static final Logger LOG = LoggerFactory.getLogger(PIDResetter.class);

  public static void main(String[] args) throws ConfigurationException {
    Injector injector = ToolsInjectionModule.createInjectorWithoutSolr();

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
        if (version.getPid() != null) {
          try {
            persistenceWrapper.updatePID(version);
          } catch (PersistenceException e) {
            LOG.error("Could not reset PID \"{}\" of type \"{}\" with id \"{}\" and revision \"{}\"", version.getPid(), type, version.getId(), version.getRev());
          }
        }
      }
    }

    progress.done();

  }
}
