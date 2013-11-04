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

  Producer newProducer(String queue, String name) throws JMSException;

  Consumer newConsumer(String queue, String name) throws JMSException;

  Browser newBrowser(String queue) throws JMSException;

  void start() throws JMSException;

  void close();

}
