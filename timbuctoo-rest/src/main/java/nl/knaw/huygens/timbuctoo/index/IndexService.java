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
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequestFactory;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequests;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.ConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;

public class IndexService extends ConsumerService implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(IndexService.class);

  private final IndexRequests indexRequests;
  private final IndexerFactory indexerFactory;
  private final IndexRequestFactory indexRequestFactory;

  @Inject
  public IndexService(Broker broker, IndexRequests indexRequests, IndexRequestFactory indexRequestFactory, IndexerFactory indexerFactory) throws JMSException {
    super(broker, Broker.INDEX_QUEUE, "IndexService");
    this.indexRequests = indexRequests;
    this.indexerFactory = indexerFactory;
    this.indexRequestFactory = indexRequestFactory;
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
    if (!action.isForMultiEntities()) {
      IndexRequest indexRequest = getIndexRequest(action);

      Indexer indexer = indexerFactory.create(action.getActionType());

      try {
        indexRequest.execute(indexer);
      } catch (IndexException e) {
        getLogger().error("Error indexing ([{}]) object of type [{}]", action.getActionType(), indexRequest.getType());
        getLogger().debug("Exception while indexing", e);
      }
    }
  }

  private IndexRequest getIndexRequest(Action action) {
    IndexRequest indexRequest;
    if (action.hasRequestId()) {
      indexRequest = indexRequests.get(action.getRequestId());
    } else {
      indexRequest = indexRequestFactory.forEntity(action.getType(), action.getId());
    }
    return indexRequest;
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
