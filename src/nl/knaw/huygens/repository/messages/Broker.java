package nl.knaw.huygens.repository.messages;

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

  public static final String INDEX_QUEUE = "index";

  // TODO model
  public static final String INDEX_ADD = "add"; // add item
  public static final String INDEX_DEL = "del"; // delete item
  public static final String INDEX_MOD = "mod"; // update item
  public static final String INDEX_END = "end"; // stop processing

  private final String url;

  @Inject
  public Broker(@Named("messages.broker_url") String url) {
    System.out.printf("Message broker URL: '%s'%n", url);
    this.url = url;
  }

  public Producer newProducer(String queue) throws JMSException {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
    return new Producer(factory, queue);
  }

  public Consumer newConsumer(String queue, MessageListener listener) throws JMSException {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
    return new Consumer(factory, queue, listener);
  }

  public Browser newBrowser(String queue) throws JMSException {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
    return new Browser(factory, queue);
  }

  public void close() throws JMSException {
    System.out.println("... closing broker");
  }

}
