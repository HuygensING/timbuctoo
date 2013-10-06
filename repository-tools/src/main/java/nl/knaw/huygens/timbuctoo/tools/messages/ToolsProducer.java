package nl.knaw.huygens.timbuctoo.tools.messages;

import javax.jms.JMSException;

import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToolsProducer implements Producer {

  private static final Logger LOG = LoggerFactory.getLogger(ToolsProducer.class);

  private final ToolsConsumer toolsConsumer;

  public ToolsProducer(ToolsConsumer toolsConsumer) {
    this.toolsConsumer = toolsConsumer;
  }

  @Override
  public void send(ActionType action, String type, String id) throws JMSException {
    toolsConsumer.addAction(action, type, id);
  }

  @Override
  public void close() throws JMSException {
    // Method not needed, only needed when the consumer uses with jms.
    LOG.info("Closing producer");
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

}
