package nl.knaw.huygens.timbuctoo.index;

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

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.ConsumerService;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;

public class IndexService extends ConsumerService implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(IndexService.class);

  private final IndexManager manager;
  private final IndexRequests indexRequests;
  private final IndexerFactory indexerFactory;

  @Inject
  public IndexService(IndexManager manager, Broker broker, IndexRequests indexRequests, IndexerFactory indexerFactory) throws JMSException {
    super(broker, Broker.INDEX_QUEUE, "IndexService");
    this.manager = manager;
    this.indexRequests = indexRequests;
    this.indexerFactory = indexerFactory;
  }

  /**
   * Needed to make it possible to log with the right Logger in the superclass;
   *
   * @return
   */
  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  protected void executeAction(Action action) {
    // ignore multiple entity actions for now
    if(action.hasRequestId()){
      executeIndexRequestWithRequestId(action);
    }

    executeSimpleIndexRequest(action);
  }

  private void executeIndexRequestWithRequestId(Action action) {
    IndexRequest indexRequest = indexRequests.get(action.getRequestId());
    Indexer indexer = indexerFactory.create(action.getActionType());

    try {
      indexer.executeFor(indexRequest);
    } catch (IndexException e) {
      getLogger().error("Error indexing ({}) object of type {}", action.getActionType(), indexRequest.getType());
      getLogger().debug("Exception while indexing", e);    }
  }

  private void executeSimpleIndexRequest(Action action) {
    if (!action.isForMultiEntities()) {
      ActionType actionType = action.getActionType();
      Class<? extends DomainEntity> type = action.getType();
      String id = action.getId();

      try {
        switch (actionType) {
          case ADD:
            manager.addEntity(type, id);
            break;
          case MOD:
            manager.updateEntity(type, id);
            break;
          case DEL:
            manager.deleteEntity(type, id);
            break;
          case END:
            this.stop(); //stop the Runnable
        }
      } catch (IndexException ex) {
        getLogger().error("Error indexing ({}) object of type {} with id {}", actionType, type, id);
        getLogger().debug("Exception while indexing", ex);
      }
    }
  }

  public static void waitForCompletion(Thread thread, long patience) {
    try {
      long targetTime = System.currentTimeMillis() + patience;
      while (thread.isAlive()) {
        LOG.info("Waiting...");
        thread.join(2000);
        if (System.currentTimeMillis() > targetTime && thread.isAlive()) {
          LOG.info("Tired of waiting!");
          thread.interrupt();
          thread.join();
        }
      }
    } catch (InterruptedException e) {
      // Just log. Give other services a chance to close...
      LOG.error(e.getMessage(), e);
    }
  }

}
