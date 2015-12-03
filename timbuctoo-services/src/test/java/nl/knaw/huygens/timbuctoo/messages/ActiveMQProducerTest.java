package nl.knaw.huygens.timbuctoo.messages;

/*
 * #%L
 * Timbuctoo services
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

import org.junit.Before;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ActiveMQProducerTest {

  public static final String QUEUE = "queue";
  public static final String NAME = "name";
  private MessageProducer messageProducer;
  private ActiveMQProducer instance;
  private Session session;

  @Before
  public void setup() throws JMSException {
    ConnectionFactory connectionFactory = setupConnectionFactory();
    instance = new ActiveMQProducer(connectionFactory, QUEUE, NAME);
  }

  private ConnectionFactory setupConnectionFactory() throws JMSException {
    session = mock(Session.class);
    messageProducer = mock(MessageProducer.class);
    when(session.createProducer(any(Destination.class))).thenReturn(messageProducer);

    Connection connection = mock(Connection.class);
    when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);

    ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
    when(connectionFactory.createConnection()).thenReturn(connection);

    return connectionFactory;
  }

  @Test
  public void sendSendsAByActionCreatedMessage() throws JMSException {
    // setup
    Message message = mock(Message.class);
    Action action = mock(Action.class);
    when(action.createMessage(session)).thenReturn(message);

    // action
    instance.send(action);

    // verify
    verify(messageProducer).send(message);
  }

  @Test(expected = JMSException.class)
  public void sendThrowsAJMSExceptionWhenTheMessageProducerDoes() throws JMSException {
    // setup
    Message message = mock(Message.class);
    Action action = mock(Action.class);
    when(action.createMessage(session)).thenReturn(message);

    doThrow(JMSException.class).when(messageProducer).send(message);

    // action
    instance.send(action);
  }

}
