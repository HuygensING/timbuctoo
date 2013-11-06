package nl.knaw.huygens.timbuctoo.messages;

import javax.jms.JMSException;

import org.slf4j.Logger;

public abstract class ConsumerService implements Runnable {

  protected final Consumer consumer;

  protected abstract void executeAction(Action action);

  protected abstract Logger getLogger();

  private volatile boolean running;

  public ConsumerService(Broker broker, String queue, String consumerName) throws JMSException {
    consumer = createConsumer(broker, queue, consumerName);
  }

  public void stop() {
    running = false;
  }

  @Override
  public void run() {
    getLogger().info("Started");
    running = true;
    while (running) {
      try {
        Action action = consumer.receive();
        if (action != null) {
          executeAction(action);
        }
      } catch (JMSException e) {
        getLogger().error("Exception while receiving data.", e);
      }
    }
    consumer.closeQuietly();
    getLogger().info("Stopped");
  }

  protected Consumer createConsumer(Broker broker, String queueName, String consumerName) throws JMSException {
    try {
      return broker.getConsumer(consumerName, queueName);
    } catch (JMSException e) {
      getLogger().error("Failed to create consumer {} for queue {}", consumerName, queueName);
      getLogger().debug("Exception thrown while creating a new consumer.", e);
      throw e;
    }
  }

}
