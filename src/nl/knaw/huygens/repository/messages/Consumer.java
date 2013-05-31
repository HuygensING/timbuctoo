package nl.knaw.huygens.repository.messages;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

public class Consumer {

  private final String name;
  private Connection connection;
  private Session session;
  private MessageConsumer consumer;

  public Consumer(ConnectionFactory factory, String queue, String name) throws JMSException {
    this.name = name;
    connection = factory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Destination destination = session.createQueue(queue);
    consumer = session.createConsumer(destination);
  }

  public Message receive() throws JMSException {
    return consumer.receive(1000);
  }

  public void close() throws JMSException {
    System.out.format("... closing message consumer '%s'%n", name);
    session.close();
    connection.close();
  }

  public void closeQuietly() {
    try {
      close();
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }

}
