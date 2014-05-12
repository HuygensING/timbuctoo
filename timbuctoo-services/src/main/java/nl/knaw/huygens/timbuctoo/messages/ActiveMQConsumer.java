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
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveMQConsumer implements Consumer {

  private static final Logger LOG = LoggerFactory.getLogger(ActiveMQConsumer.class);

  private final String name;
  private final TypeRegistry typeRegistry;
  private Connection connection;
  private Session session;
  private MessageConsumer consumer;

  public ActiveMQConsumer(ConnectionFactory factory, String queue, String name, TypeRegistry typeRegistry) throws JMSException {
    this.name = name;
    this.typeRegistry = typeRegistry;
    LOG.info("Creating '{}'", name);
    connection = factory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Destination destination = session.createQueue(queue);
    consumer = session.createConsumer(destination);
    LOG.info("Created '{}'", name);
  }

  @Override
  public Action receive() throws JMSException {
    Message message = consumer.receive(1000);
    if (message == null) {
      return null;
    }

    String action = message.getStringProperty(Broker.PROP_ACTION);
    ActionType actionType = ActionType.getFromString(action);

    String typeString = message.getStringProperty(Broker.PROP_DOC_TYPE);
    Class<? extends DomainEntity> type = getType(typeString);

    String id = message.getStringProperty(Broker.PROP_DOC_ID);

    return new Action(actionType, type, id);
  }

  private Class<? extends DomainEntity> getType(String typeString) throws JMSException {
    Class<? extends DomainEntity> type = typeRegistry.getDomainEntityType(typeString);
    if (type == null) {
      throw new JMSException("Unknown type: " + typeString);
    }
    return type;
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
