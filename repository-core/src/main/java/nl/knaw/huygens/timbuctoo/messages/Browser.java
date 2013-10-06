package nl.knaw.huygens.timbuctoo.messages;

import javax.jms.JMSException;

public interface Browser {

  public abstract boolean hasMoreElements();

  public abstract int numElements();

  public abstract void close() throws JMSException;

}
