package nl.knaw.huygens.timbuctoo.messages;

import javax.jms.JMSException;

import nl.knaw.huygens.timbuctoo.model.Entity;

public interface Producer {

  void send(ActionType action, Class<? extends Entity> type, String id) throws JMSException;

  void close() throws JMSException;

  void closeQuietly();

}
