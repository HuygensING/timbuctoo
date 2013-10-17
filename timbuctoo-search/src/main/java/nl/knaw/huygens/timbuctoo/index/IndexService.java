package nl.knaw.huygens.timbuctoo.index;

import javax.jms.JMSException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Consumer;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

// TODO handle exceptions properly
public class IndexService implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(IndexService.class);

  private final IndexManager manager;
  private final Consumer consumer;
  private TypeRegistry registry;

  private volatile boolean running;

  @Inject
  public IndexService(IndexManager manager, Broker broker, TypeRegistry registry) throws JMSException {
    this.manager = manager;
    this.consumer = broker.newConsumer(Broker.INDEX_QUEUE, "IndexServiceConsumer");
    this.registry = registry;
  }

  public void stop() {
    running = false;
  }

  @Override
  public void run() {
    LOG.info("Started");
    running = true;
    RUN_LOOP: while (running) {
      try {
        Action action = consumer.receive();
        if (action != null) {
          ActionType actionType = action.getActionType();
          String typeString = action.getTypeString();
          Class<? extends Entity> type = registry.getTypeForIName(typeString);
          String id = action.getId();

          switch (actionType) {
          case INDEX_ADD:
            manager.addDocument(type, id);
            break;
          case INDEX_MOD:
            manager.updateDocument(type, id);
            break;
          case INDEX_DEL:
            manager.deleteDocument(type, id);
            break;
          case INDEX_END:
            break RUN_LOOP; //break the while loop
          }
        }
      } catch (JMSException e) {
        e.printStackTrace();
      } catch (IndexException e) {
        e.printStackTrace();
      }
    }
    consumer.closeQuietly();
    LOG.info("Stopped");
  }

  public static void waitForCompletion(Thread thread, long patience) {
    try {
      long targetTime = System.currentTimeMillis() + patience;
      while (thread.isAlive()) {
        LOG.info("Waiting...");
        thread.join(2000);
        if (System.currentTimeMillis() > targetTime && thread.isAlive()) {
          LOG.info("Tired of waiting!");
          thread.interrupt();
          thread.join();
        }
      }
    } catch (InterruptedException e) {
      // Just log. Give other services a chance to close...
      LOG.error(e.getMessage(), e);
    }
  }

}
