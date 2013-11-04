package nl.knaw.huygens.timbuctoo.messages;

import javax.jms.JMSException;

public interface Consumer {

  Action receive() throws JMSException;

  void close() throws JMSException;

  void closeQuietly();

}
