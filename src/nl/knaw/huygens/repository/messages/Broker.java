package nl.knaw.huygens.repository.messages;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Encapsulates an embedded ActiveMQ message broker.
 */
@Singleton
public class Broker {

  private static final Logger LOG = LoggerFactory.getLogger(Broker.class);

  public static final String INDEX_QUEUE = "index";

  // Message headers
  public static final String PROP_ACTION = "action";
  public static final String PROP_DOC_TYPE = "type";
  public static final String PROP_DOC_ID = "id";

  // Index actions
  public static final String INDEX_ADD = "add"; // add item
  public static final String INDEX_DEL = "del"; // delete item
  public static final String INDEX_MOD = "mod"; // update item
  public static final String INDEX_END = "end"; // stop processing

  private final String url;

  @Inject
  public Broker(@Named("messages.broker_url") String url) {
    LOG.info("Message broker URL: '{}'", url);
    this.url = url;
  }

  public Producer newProducer(String queue, String name) throws JMSException {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
    return new Producer(factory, queue, name);
  }

  public Consumer newConsumer(String queue, String name) throws JMSException {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
    return new Consumer(factory, queue, name);
  }

  public Browser newBrowser(String queue) throws JMSException {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
    return new Browser(factory, queue);
  }

  public void close() throws JMSException {
    LOG.info("Closing");
  }

}
