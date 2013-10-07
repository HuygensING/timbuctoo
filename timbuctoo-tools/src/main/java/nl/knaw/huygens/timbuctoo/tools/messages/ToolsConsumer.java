package nl.knaw.huygens.timbuctoo.tools.messages;

import java.util.Queue;

import javax.jms.JMSException;

import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Queues;

public class ToolsConsumer implements Consumer {

  private static final Logger LOG = LoggerFactory.getLogger(ToolsConsumer.class);

  private final Queue<Action> actions;

  public ToolsConsumer() {
    actions = Queues.newLinkedBlockingQueue();
  }

  @Override
  public Action receive() throws JMSException {
    synchronized (this) {
      if (actions.isEmpty()) {
        return null;
      }
      return actions.remove();
    }
  }

  @Override
  public void close() throws JMSException {
    // Method not needed, only needed when the consumer uses with jms.
    LOG.info("Closing consumer");
  }

  @Override
  public void closeQuietly() {
    // Method not needed, only needed when the consumer uses with jms.
    try {
      this.close();
    } catch (JMSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * A method, that provides the logic to directly send messages to the consumer.
   * @param action
   * @param type
   * @param id
   */
  synchronized void addAction(ActionType action, String type, String id) {
    actions.add(new Action(action, type, id));
  }
}
