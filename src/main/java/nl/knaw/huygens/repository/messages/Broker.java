package nl.knaw.huygens.repository.messages;

import javax.jms.JMSException;

import nl.knaw.huygens.repository.config.Configuration;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.StoreUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Encapsulates an embedded ActiveMQ message broker.
 */
@Singleton
public class Broker {

  private static final Logger LOG = LoggerFactory.getLogger(Broker.class);

  public static final String BROKER_NAME = "repo-broker";
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

  private BrokerService brokerService;

  @Inject
  public Broker(Configuration config) {
    url = "vm://" + BROKER_NAME;
    LOG.info("Message broker URL: '{}'", url);
    createBrokerService(config);
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

  private static final String KEY_PERSISTENT = "messages.persistent";
  private static final String KEY_MEMORY = "messages.system_usage.memory_mb";
  private static final String KEY_STORE = "messages.system_usage.store_mb";
  private static final String KEY_TEMP = "messages.system_usage.temp_mb";
  private static long MEGA_BYTE = 1024 * 1024;

  /**
   * Creates an embeded message broker
   */
  private void createBrokerService(Configuration config) {
    brokerService = new BrokerService();
    brokerService.setBrokerName(BROKER_NAME);
    brokerService.setPersistent(config.getBooleanSetting(KEY_PERSISTENT, false));

    SystemUsage systemManager = new SystemUsage();
    MemoryUsage memoryUsage = new MemoryUsage();
    memoryUsage.setLimit(config.getIntSetting(KEY_MEMORY, 10) * MEGA_BYTE);
    systemManager.setMemoryUsage(memoryUsage);

    StoreUsage storeUsage = new StoreUsage();
    storeUsage.setLimit(config.getIntSetting(KEY_STORE, 100) * MEGA_BYTE);
    systemManager.setStoreUsage(storeUsage);

    TempUsage tempUsage = new TempUsage();
    tempUsage.setLimit(config.getIntSetting(KEY_TEMP, 20) * MEGA_BYTE);
    systemManager.setTempUsage(tempUsage);

    brokerService.setSystemUsage(systemManager);
  }

  public void start() throws JMSException {
    try {
      brokerService.start();
    } catch (Exception e) {
      LOG.error("Failed to start broker service", e);
      throw new JMSException(e.getMessage());
    }
  }

  public void close() {
    if (brokerService != null) {
      LOG.info("Closing");
      try {
        brokerService.stop();
      } catch (Exception e) {
        LOG.error("Failed to close broker service", e);
        // Allow other services to close, if possible
      } finally {
        brokerService = null;
      }
    }
  }

}
