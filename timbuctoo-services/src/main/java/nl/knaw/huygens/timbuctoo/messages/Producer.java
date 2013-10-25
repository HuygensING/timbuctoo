package nl.knaw.huygens.timbuctoo.messages;

import javax.jms.JMSException;

import nl.knaw.huygens.timbuctoo.model.Entity;

public interface Producer {

  public abstract void send(ActionType action, Class<? extends Entity> type, String id) throws JMSException;

  public abstract void close() throws JMSException;

  public abstract void closeQuietly();

}
