package nl.knaw.huygens.timbuctoo.messages;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveMQConsumer implements Consumer {

  private static final Logger LOG = LoggerFactory.getLogger(ActiveMQConsumer.class);

  private final String name;
  private Connection connection;
  private Session session;
  private MessageConsumer consumer;

  public ActiveMQConsumer(ConnectionFactory factory, String queue, String name) throws JMSException {
    this.name = name;
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
    String id = message.getStringProperty(Broker.PROP_DOC_ID);

    return new Action(actionType, typeString, id);
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
