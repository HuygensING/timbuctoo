package nl.knaw.huygens.timbuctoo.index;

import javax.jms.JMSException;

import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.ConsumerService;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class IndexService extends ConsumerService implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(IndexService.class);

  private final IndexManager manager;

  @Inject
  public IndexService(IndexManager manager, Broker broker) throws JMSException {
    super(broker, Broker.INDEX_QUEUE, "IndexService");
    this.manager = manager;
  }

  /**
   * Needed to make it possible to log with the right Logger in the superclass; 
   * @return
   */
  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  protected void executeAction(Action action) {
    ActionType actionType = action.getActionType();
    Class<? extends Entity> type = action.getType();
    String id = action.getId();

    try {
      switch (actionType) {
      case ADD:
        manager.addEntity(type, id);
        break;
      case MOD:
        manager.updateEntity(type, id);
        break;
      case DEL:
        manager.deleteEntity(type, id);
        break;
      case END:
        this.stop(); //stop the Runnable
      }
    } catch (IndexException ex) {
      getLogger().error("Error indexing ({}) object of type {} with id {}", new Object[] { actionType, type, id });
      getLogger().debug("Exception while indexing", ex);
    }
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
