package nl.knaw.huygens.timbuctoo.messages;

import java.util.Map;

import javax.jms.JMSException;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.StoreUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Encapsulates an embedded ActiveMQ message broker.
 */
@Singleton
public class ActiveMQBroker implements Broker {

  private static final Logger LOG = LoggerFactory.getLogger(ActiveMQBroker.class);

  private final String url;
  private final TypeRegistry typeRegistry;

  private BrokerService brokerService;
  private Map<String, Producer> producers;
  private Map<String, Consumer> consumers;

  @Inject
  //TODO factor out the config.
  public ActiveMQBroker(Configuration config, TypeRegistry typeRegistry) {
    url = "vm://" + BROKER_NAME;
    LOG.info("Message broker URL: '{}'", url);
    createBrokerService(config);
    this.typeRegistry = typeRegistry;

    producers = Maps.newTreeMap();
    consumers = Maps.newTreeMap();
  }

  @Override
  public Producer getProducer(String name, String queue) throws JMSException {
    Producer producer = producers.get(name);
    if (producer == null) {
      producer = newProducer(name, queue);
      producers.put(name, producer);
      LOG.info("Number of producers: {}", producers.size());
    }
    return producer;
  }

  private Producer newProducer(String name, String queue) throws JMSException {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
    return new ActiveMQProducer(factory, queue, name, typeRegistry);
  }

  @Override
  public Consumer getConsumer(String name, String queue) throws JMSException {
    Consumer consumer = consumers.get(name);
    if (consumer == null) {
      consumer = newConsumer(name, queue);
      consumers.put(name, consumer);
      LOG.info("Number of consumers: {}", consumers.size());
    }
    return consumer;
  }

  private Consumer newConsumer(String name, String queue) throws JMSException {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
    return new ActiveMQConsumer(factory, queue, name, typeRegistry);
  }

  @Override
  public Browser newBrowser(String queue) throws JMSException {
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
    return new ActiveMQBrowser(factory, queue);
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

  @Override
  public void start() throws JMSException {
    try {
      brokerService.start();
    } catch (Exception e) {
      LOG.error("Failed to start broker service", e);
      throw new JMSException(e.getMessage());
    }
  }

  @Override
  public void close() {
    if (brokerService != null) {
      closeProducers();
      closeConsumers();

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

  private void closeProducers() {
    LOG.info("Closing producers");
    for (Producer producer : producers.values()) {
      producer.closeQuietly();
    }
    producers.clear();
  }

  private void closeConsumers() {
    LOG.info("Closing consumers");
    for (Consumer consumer : consumers.values()) {
      consumer.closeQuietly();
    }
    consumers.clear();
  }

}
