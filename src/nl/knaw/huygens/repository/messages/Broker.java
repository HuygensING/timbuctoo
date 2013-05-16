package nl.knaw.huygens.repository.messages;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageListener;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Encapsulates an embedded ActiveMQ message broker.
 */
@Singleton
public class Broker {

  private final ConnectionFactory factory;

  @Inject
  public Broker(@Named("messages.broker_url") String url) {
    System.out.printf("Message broker URL: '%s'%n", url);
    factory = new ActiveMQConnectionFactory(url);
  }

  public Producer newProducer(String queue) throws JMSException {
    return new Producer(factory, queue);
  }

  public Consumer newConsumer(String queue, MessageListener listener) throws JMSException {
    return new Consumer(factory, queue, listener);
  }

  public void close() throws JMSException {
    System.out.println("Closing broker");
  }

}
