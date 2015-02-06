package nl.knaw.huygens.timbuctoo.rest.resources;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
