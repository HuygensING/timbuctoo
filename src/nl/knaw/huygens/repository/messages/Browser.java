package nl.knaw.huygens.repository.messages;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

public class Browser {

  private Connection connection;
  private Session session;
  private QueueBrowser browser;

  public Browser(ConnectionFactory factory, String queue) throws JMSException {
    connection = factory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Queue destination = session.createQueue(queue);
    browser = session.createBrowser(destination);
  }

  public boolean hasMoreElements() {
    try {
      Enumeration<?> enumeration = browser.getEnumeration();
      return enumeration.hasMoreElements();
    } catch (JMSException e) {
      return false;
    }
  }

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

  public void close() throws JMSException {
    browser.close();
    session.close();
    connection.close();
  }

}
