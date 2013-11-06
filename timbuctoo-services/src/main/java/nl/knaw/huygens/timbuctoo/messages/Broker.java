package nl.knaw.huygens.timbuctoo.messages;

import javax.jms.JMSException;

public interface Broker {

  String BROKER_NAME = "repo-broker";
  String INDEX_QUEUE = "index";
  String PERSIST_QUEUE = "persist";
  // Message headers
  String PROP_ACTION = "action";
  String PROP_DOC_TYPE = "type";
  String PROP_DOC_ID = "id";

  /**
   * Returns the message producer with the specified name that writes
   * messages to the specified queue, creating it if it does not exist.
   */
  Producer getProducer(String name, String queue) throws JMSException;

  /**
   * Returns the message consumer with the specified name that reads
   * messages from the specified queue, creating it if it does not exist.
   */
  Consumer getConsumer(String name, String queue) throws JMSException;

  Browser newBrowser(String queue) throws JMSException;

  void start() throws JMSException;

  void close();

}
