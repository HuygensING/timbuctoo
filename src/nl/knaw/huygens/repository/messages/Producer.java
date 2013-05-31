package nl.knaw.huygens.repository.messages;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

public class Producer {

  private final String name;
  private Connection connection;
  private Session session;
  private MessageProducer producer;

  public Producer(ConnectionFactory factory, String queue, String name) throws JMSException {
    this.name = name;
    connection = factory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Destination destination = session.createQueue(queue);
    producer = session.createProducer(destination);
    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
  }

  public void send(String action, String type, String id) throws JMSException {
    Message message = session.createMessage();
    message.setStringProperty(Broker.PROP_ACTION, action);
    message.setStringProperty(Broker.PROP_DOC_TYPE, type);
    message.setStringProperty(Broker.PROP_DOC_ID, id);
    producer.send(message);
  }

  public void close() throws JMSException {
    System.out.format("... closing message producer '%s'%n", name);
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
