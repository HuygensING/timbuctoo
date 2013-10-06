package nl.knaw.huygens.timbuctoo.messages;

import javax.jms.JMSException;

public interface Producer {

  public abstract void send(ActionType action, String type, String id) throws JMSException;

  public abstract void close() throws JMSException;

  public abstract void closeQuietly();

}
