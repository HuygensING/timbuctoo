package nl.knaw.huygens.repository.messages;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Consumer {

  private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);

  private final String name;
  private Connection connection;
  private Session session;
  private MessageConsumer consumer;

  public Consumer(ConnectionFactory factory, String queue, String name) throws JMSException {
    this.name = name;
    LOG.info("Creating '{}'", name);
    connection = factory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Destination destination = session.createQueue(queue);
    consumer = session.createConsumer(destination);
    LOG.info("Created '{}'", name);
  }

  public Message receive() throws JMSException {
    return consumer.receive(1000);
  }

  public void close() throws JMSException {
    LOG.info("Closing '{}'", name);
    session.close();
    connection.close();
  }

  public void closeQuietly() {
    try {
      close();
    } catch (JMSException e) {
      LOG.error("Error while closing", e);
    }
  }

}
