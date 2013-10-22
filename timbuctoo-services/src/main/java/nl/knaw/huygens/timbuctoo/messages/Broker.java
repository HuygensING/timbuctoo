package nl.knaw.huygens.timbuctoo.messages;

import javax.jms.JMSException;

public interface Broker {

  public static final String BROKER_NAME = "repo-broker";
  public static final String INDEX_QUEUE = "index";
  // Message headers
  public static final String PROP_ACTION = "action";
  public static final String PROP_DOC_TYPE = "type";
  public static final String PROP_DOC_ID = "id";

  public abstract Producer newProducer(String queue, String name) throws JMSException;

  public abstract Consumer newConsumer(String queue, String name) throws JMSException;

  public abstract Browser newBrowser(String queue) throws JMSException;

  public abstract void start() throws JMSException;

  public abstract void close();

}
