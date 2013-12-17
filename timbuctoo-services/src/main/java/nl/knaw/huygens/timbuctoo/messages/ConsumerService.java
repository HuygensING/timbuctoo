package nl.knaw.huygens.timbuctoo.messages;

/*
 * #%L
 * Timbuctoo services
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import javax.jms.JMSException;

import org.slf4j.Logger;

public abstract class ConsumerService implements Runnable {

  protected final Consumer consumer;

  protected abstract void executeAction(Action action);

  protected abstract Logger getLogger();

  private volatile boolean running;

  public ConsumerService(Broker broker, String queue, String consumerName) throws JMSException {
    consumer = createConsumer(broker, queue, consumerName);
  }

  public void stop() {
    running = false;
  }

  @Override
  public void run() {
    getLogger().info("Started");
    running = true;
    while (running) {
      try {
        Action action = consumer.receive();
        if (action != null) {
          executeAction(action);
        }
      } catch (JMSException e) {
        getLogger().error("Exception while receiving data.", e);
      }
    }
    consumer.closeQuietly();
    getLogger().info("Stopped");
  }

  protected Consumer createConsumer(Broker broker, String queueName, String consumerName) throws JMSException {
    try {
      return broker.getConsumer(consumerName, queueName);
    } catch (JMSException e) {
      getLogger().error("Failed to create consumer {} for queue {}", consumerName, queueName);
      getLogger().debug("Exception thrown while creating a new consumer.", e);
      throw e;
    }
  }

}
