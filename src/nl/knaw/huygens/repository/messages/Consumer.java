package nl.knaw.huygens.repository.messages;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

public class Consumer {

  private Connection connection;
  private Session session;

  public Consumer(ConnectionFactory factory, String queue, MessageListener listener) throws JMSException {
    connection = factory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Destination destination = session.createQueue(queue);
    MessageConsumer consumer = session.createConsumer(destination);
    consumer.setMessageListener(listener);
  }

  public void close() throws JMSException {
    session.close();
    connection.close();
  }

}
