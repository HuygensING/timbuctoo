package nl.knaw.huygens.timbuctoo.persistence;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import java.io.IOException;

import javax.jms.JMSException;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.ConsumerService;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class PersistenceService extends ConsumerService implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(PersistenceService.class);

  private final PersistenceWrapper persistenceWrapper;
  private final StorageManager storageManager;

  @Inject
  public PersistenceService(Broker broker, PersistenceWrapper persistenceWrapper, StorageManager storageManager) throws JMSException {
    super(broker, Broker.PERSIST_QUEUE, "PersistenceService");
    this.persistenceWrapper = persistenceWrapper;
    this.storageManager = storageManager;
  }

  @Override
  protected void executeAction(Action action) {

    switch (action.getActionType()) {
    case ADD:
    case MOD:
      setPID(action);
      break;
    case DEL:
      LOG.debug("Ignoring action {}", action);
      break;
    default:
      LOG.warn("Unexpected action {}", action);
      break;
    }

  }

  private void setPID(Action action) {
    Class<? extends Entity> type = action.getType();
    String id = action.getId();
    String pid = null;

    if (!TypeRegistry.isDomainEntity(type)) {
      LOG.error("Not a domain entitiy: {}", type.getSimpleName());
      return;
    }

    int revision = getRevision(type, id);

    try {
      pid = persistenceWrapper.persistObject(type, id, revision);
    } catch (PersistenceException ex) {
      LOG.error("Creating a PID for {} with id {} went wrong.", type, id);
      LOG.debug("Exception", ex);
      return;
    }

    try {
      storageManager.setPID(TypeRegistry.toDomainEntity(type), id, pid);
    } catch (IllegalStateException ex) {
      deletePID(pid);
      LOG.error("{} with id {} already has a PID", type, id);
    } catch (IOException ex) {
      deletePID(pid);
      LOG.error("Persisting {} with id {} went wrong", type, id);
      LOG.debug("Exception", ex);
    }
  }

  private void deletePID(String pid) {
    try {
      persistenceWrapper.deletePersistentId(pid);
    } catch (PersistenceException pe) {
      LOG.error("Deleting PID {} went wrong.", pid);
      LOG.debug("Exception", pe);
    }
  }

  private <T extends Entity> int getRevision(Class<T> type, String id) {
    T instance = storageManager.getEntity(type, id);

    return instance.getRev();
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

}
