package nl.knaw.huygens.repository.messages;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class Producer {

  private Connection connection;
  private Session session;
  private MessageProducer producer;

  public Producer(ConnectionFactory factory, String queue) throws JMSException {
    connection = factory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Destination destination = session.createQueue(queue);
    producer = session.createProducer(destination);
    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
  }

  public void send(String text) throws JMSException {
    TextMessage message = session.createTextMessage(text);
    producer.send(message);
  }

  public void send(String action, String type, String id) throws JMSException {
    Message message = session.createMessage();
    message.setStringProperty("action", action);
    message.setStringProperty("type", type);
    message.setStringProperty("id", id);
    producer.send(message);
  }

  public void close() throws JMSException {
    session.close();
    connection.close();
  }

}
