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
import nl.knaw.huygens.timbuctoo.index.request.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequestFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.ConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;

public class IndexService extends ConsumerService implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(IndexService.class);
  private static final int FIVE_SECONDS = 5000;

  private final IndexRequestFactory indexRequestFactory;
  private final int timeout;

  @Inject
  public IndexService(Broker broker, IndexRequestFactory indexRequestFactory) throws JMSException {
    this(broker, indexRequestFactory, FIVE_SECONDS);
  }

  // a constructor for the tests to be able to shorten the timeout
  IndexService(Broker broker, IndexRequestFactory indexRequestFactory, int timeout) throws JMSException {
    super(broker, Broker.INDEX_QUEUE, "IndexService");
    this.indexRequestFactory = indexRequestFactory;
    this.timeout = timeout;
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
    IndexRequest indexRequest = indexRequestFactory.forAction(action);

    boolean shouldExecute = true;
    int numberOfTries = 0;
    while (shouldExecute && numberOfTries < 5) {
      try {
        LOG.info("Processing index request \"{}\"", indexRequest);
        indexRequest.execute();
        shouldExecute = false;
      } catch (IndexException | RuntimeException e) {
        getLogger().error("Error executing index request \"{}\"", indexRequest);
        getLogger().error("Exception while indexing", e);

        numberOfTries += 1;
        try {
          Thread.sleep(timeout);
        } catch (InterruptedException e1) {
          getLogger().warn("Sleep interrupted.", e1);
        }
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
