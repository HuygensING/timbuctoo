package nl.knaw.huygens.timbuctoo.messages;

import javax.jms.JMSException;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public interface Producer {

  void send(ActionType action, Class<? extends DomainEntity> type, String id) throws JMSException;

  void close() throws JMSException;

  void closeQuietly();

}
