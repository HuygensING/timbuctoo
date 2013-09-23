package nl.knaw.huygens.repository.messages;

import javax.jms.JMSException;

public interface Producer {

  public abstract void send(String action, String type, String id) throws JMSException;

  public abstract void close() throws JMSException;

  public abstract void closeQuietly();

}