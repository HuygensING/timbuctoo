package nl.knaw.huygens.timbuctoo.rest.resources;

import javax.jms.JMSException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ChangeHelper {
  public static final String INDEX_MSG_PRODUCER = "ResourceIndexProducer";
  public static final String PERSIST_MSG_PRODUCER = "ResourcePersistProducer";
  private static final Logger LOG = LoggerFactory.getLogger(ChangeHelper.class);

  private final TypeRegistry typeRegistry;
  private final Broker broker;

  @Inject
  public ChangeHelper(Broker broker, TypeRegistry typeRegistry) {
    this.broker = broker;
    this.typeRegistry = typeRegistry;
  }

  /**
   * Notify other software components of a change in the data.
   */
  public void notifyChange(ActionType actionType, Class<? extends DomainEntity> type, DomainEntity entity, String id) {
    switch (actionType) {
      case ADD:
      case MOD:
        sendPersistMessage(actionType, type, id);
        sendIndexMessage(actionType, type, id);
        break;
      case DEL:
        sendIndexMessage(actionType, type, id);
        break;
      default:
        LOG.error("Unexpected action {}", actionType);
        break;
    }

    // TODO improve this solution
    if (Relation.class.isAssignableFrom(type)) {
      Relation relation = (Relation) entity;
      updateIndex(relation.getSourceType(), relation.getSourceId());
      updateIndex(relation.getTargetType(), relation.getTargetId());
    }
  }

  private void updateIndex(String iname, String id) {
    sendIndexMessage(ActionType.MOD, typeRegistry.getDomainEntityType(iname), id);
  }

  private void sendIndexMessage(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    try {
      Producer producer = broker.getProducer(INDEX_MSG_PRODUCER, Broker.INDEX_QUEUE);
      producer.send(actionType, type, id);
    } catch (JMSException e) {
      LOG.error("Failed to send index message {} - {} - {}. \n{}", actionType, type, id, e.getMessage());
      LOG.debug("Exception", e);
    }
  }

  public void sendPersistMessage(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    try {
      Producer producer = broker.getProducer(PERSIST_MSG_PRODUCER, Broker.PERSIST_QUEUE);
      producer.send(actionType, type, id);
    } catch (JMSException e) {
      LOG.error("Failed to send persistence message {} - {} - {}. \n{}", actionType, type, id, e.getMessage());
      LOG.debug("Exception", e);
    }
  }
}
