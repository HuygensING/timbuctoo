package nl.knaw.huygens.timbuctoo.messages;

/*
 * #%L
 * Timbuctoo services
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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveMQProducer implements Producer {

  private static final Logger LOG = LoggerFactory.getLogger(ActiveMQProducer.class);

  private final String name;
  private Connection connection;
  private Session session;
  private MessageProducer producer;

  public ActiveMQProducer(ConnectionFactory factory, String queue, String name) throws JMSException {
    this.name = name;
    LOG.info("Creating '{}'", name);
    connection = factory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Destination destination = session.createQueue(queue);
    producer = session.createProducer(destination);
    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    LOG.info("Created '{}'", name);
  }

  // ActiveMQ message producers are not thread-safe
  @Override
  public synchronized void send(ActionType action, Class<? extends DomainEntity> type, String id) throws JMSException {
    Message message = session.createMessage();
    message.setStringProperty(Broker.PROP_ACTION, action.getStringRepresentation());
    message.setStringProperty(Broker.PROP_DOC_TYPE, TypeNames.getInternalName(type));
    message.setStringProperty(Broker.PROP_DOC_ID, id);
    producer.send(message);
  }

  @Override
  public void close() throws JMSException {
    LOG.info("Closing '{}'", name);
    session.close();
    connection.close();
  }

  @Override
  public void closeQuietly() {
    try {
      close();
    } catch (JMSException e) {
      LOG.error("Error while closing", e);
    }
  }

}
