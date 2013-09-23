package nl.knaw.huygens.repository.messages;

import javax.jms.JMSException;

public interface Broker {

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

  public abstract Producer newProducer(String queue, String name) throws JMSException;

  public abstract Consumer newConsumer(String queue, String name) throws JMSException;

  public abstract Browser newBrowser(String queue) throws JMSException;

  public abstract void start() throws JMSException;

  public abstract void close();

}