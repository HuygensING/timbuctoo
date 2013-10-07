package nl.knaw.huygens.timbuctoo.tools.messages;

import javax.jms.JMSException;

import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Browser;
import nl.knaw.huygens.timbuctoo.messages.Consumer;
import nl.knaw.huygens.timbuctoo.messages.Producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

@Singleton
public class ToolsBroker implements Broker {

  private static final Logger LOG = LoggerFactory.getLogger(ToolsBroker.class);

  private final ToolsConsumer toolsConsumer;
  private final ToolsProducer toolsProducer;

  public ToolsBroker() {
    toolsConsumer = new ToolsConsumer();
    toolsProducer = new ToolsProducer(toolsConsumer);
  }

  @Override
  public Producer newProducer(String queue, String name) throws JMSException {
    return toolsProducer;
  }

  @Override
  public Consumer newConsumer(String queue, String name) throws JMSException {
    return toolsConsumer;
  }

  @Override
  public Browser newBrowser(String queue) throws JMSException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void start() throws JMSException {
    // TODO Auto-generated method stub

  }

  @Override
  public void close() {
    LOG.info("Closing broker");

  }

}
