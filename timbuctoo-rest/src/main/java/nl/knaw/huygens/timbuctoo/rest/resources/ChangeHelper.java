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

import com.google.inject.Inject;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;

public class ChangeHelper {
  public static final String INDEX_MSG_PRODUCER = "ResourceIndexProducer";
  public static final String PERSIST_MSG_PRODUCER = "ResourcePersistProducer";
  private static final Logger LOG = LoggerFactory.getLogger(ChangeHelper.class);

  private final TypeRegistry typeRegistry;
  private final PersistenceRequestFactory persistenceRequestFactory;
  private final Producer indexProducer;
  private final Producer persistenceProducer;

  @Inject
  public ChangeHelper(Broker broker, TypeRegistry typeRegistry, PersistenceRequestFactory persistenceRequestFactory) throws JMSException {
    this.typeRegistry = typeRegistry;
    this.persistenceRequestFactory = persistenceRequestFactory;

    indexProducer = createProducer(broker, INDEX_MSG_PRODUCER, Broker.INDEX_QUEUE);
    persistenceProducer = createProducer(broker, PERSIST_MSG_PRODUCER, Broker.PERSIST_QUEUE);

  }

  private Producer createProducer(Broker broker, String producerName, String queue) throws JMSException {
    return broker.getProducer(producerName, queue);
  }

  /**
   * Notify other software components of a change in the data.
   */
  public void notifyChange(ActionType actionType, Class<? extends DomainEntity> type, DomainEntity entity, String id) {
    switch (actionType) {
      case ADD:
      case MOD:
        sendPersistMessage(persistenceRequestFactory.forEntity(actionType, type, id));
        sendIndexMessage(actionType, type, id);
        break;
      case DEL:
        sendIndexMessage(actionType, type, id);
        break;
      default:
        LOG.error("Unexpected action {}", actionType);
        return;
    }

    // TODO improve this solution
    if (Relation.class.isAssignableFrom(type)) {
      Relation relation = (Relation) entity;
      updateIndex(relation.getSourceType(), relation.getSourceId());
      updateIndex(relation.getTargetType(), relation.getTargetId());
    }
  }

  private void updateIndex(String iName, String id) {
    sendIndexMessage(ActionType.MOD, typeRegistry.getDomainEntityType(iName), id);
  }

  private void sendIndexMessage(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    try {
      LOG.info("Queueing index request for type \"{}\" with id \"{}\"", type, id);
      indexProducer.send(new Action(actionType, type, id));
    } catch (JMSException e) {
      LOG.error("Failed to send execute message {} - {} - {}. \n{}", actionType, type, id, e.getMessage());
      LOG.debug("Exception", e);
    }
  }

  public void sendPersistMessage(PersistenceRequest persistenceRequest) {
    try {
      LOG.info("Queueing persistence request \"{}\"", persistenceRequest);
      persistenceProducer.send(persistenceRequest.toAction());
    } catch (JMSException e) {
      LOG.error("Failed to send persistence message \"{}\"  exception: \n{}", persistenceRequest, e.getMessage());
      LOG.debug("Exception", e);
    }
  }
}
