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

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

public class ActiveMQBrowser implements Browser {

  private Connection connection;
  private Session session;
  private QueueBrowser browser;

  public ActiveMQBrowser(ConnectionFactory factory, String queue) throws JMSException {
    connection = factory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Queue destination = session.createQueue(queue);
    browser = session.createBrowser(destination);
  }

  @Override
  public boolean hasMoreElements() {
    try {
      Enumeration<?> enumeration = browser.getEnumeration();
      return enumeration.hasMoreElements();
    } catch (JMSException e) {
      return false;
    }
  }

  @Override
  public int numElements() {
    try {
      int count = 0;
      Enumeration<?> enumeration = browser.getEnumeration();
      while (enumeration.hasMoreElements()) {
        enumeration.nextElement();
        count++;
      }
      return count;
    } catch (JMSException e) {
      return 0;
    }
  }

  @Override
  public void close() throws JMSException {
    browser.close();
    session.close();
    connection.close();
  }

}
