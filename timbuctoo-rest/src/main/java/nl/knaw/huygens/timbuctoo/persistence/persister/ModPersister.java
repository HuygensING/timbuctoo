package nl.knaw.huygens.timbuctoo.persistence.persister;

/*
 * #%L
 * Timbuctoo REST api
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
