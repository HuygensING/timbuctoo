package nl.knaw.huygens.timbuctoo.messages;

/*
 * #%L
 * Timbuctoo services
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

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_ACTION;
import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_ENTITY_ID;
import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_ENTITY_TYPE;
import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_FOR_MULTI_ENTITIES;
import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_REQUEST_ID;

public class Action {

  private ActionType actionType;
  private String id;
  private Class<? extends DomainEntity> type;
  private boolean forMultiEntities;

  public Action(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    this.actionType = actionType;
    this.id = id;
    this.type = type;
    this.forMultiEntities = false;
  }

  public Action(ActionType actionType, Class<? extends DomainEntity> type) {
    this.actionType = actionType;
    this.type = type;
    this.forMultiEntities = true;
  }

  public Action() {

  }

  public ActionType getActionType() {
    return actionType;
  }

  public String getId() {
    return id;
  }

  public Class<? extends DomainEntity> getType() {
    return type;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  public static Action multiUpdateActionFor(Class<? extends DomainEntity> type) {
    return new Action(ActionType.MOD, type);
  }

  public Message createMessage(Session session) throws JMSException {
    Message message = session.createMessage();

    message.setStringProperty(PROP_ACTION, actionType.getStringRepresentation());


    message.setStringProperty(PROP_ENTITY_TYPE, TypeNames.getInternalName(type));
    message.setBooleanProperty(PROP_FOR_MULTI_ENTITIES, forMultiEntities);
    if (!forMultiEntities) {
      message.setStringProperty(PROP_ENTITY_ID, id);
    }

    return message;
  }

  public boolean isForMultiEntities() {
    return forMultiEntities;
  }

  public static Action fromMessage(Message message, TypeRegistry typeRegistry) throws JMSException {
    ActionType actionType = getActionType(message);
    String requestId = message.getStringProperty(PROP_REQUEST_ID);

    boolean forMultiEntities = message.getBooleanProperty(PROP_FOR_MULTI_ENTITIES);

    String typeString = message.getStringProperty(PROP_ENTITY_TYPE);
    Class<? extends DomainEntity> type = typeRegistry.getDomainEntityType(typeString);


    if (forMultiEntities) {
      return forMultiEntities(actionType, type);
    }


    String id = message.getStringProperty(PROP_ENTITY_ID);

    return new Action(actionType, type, id);
  }

  private static ActionType getActionType(Message message) throws JMSException {
    String actionString = message.getStringProperty(PROP_ACTION);
    return ActionType.getFromString(actionString);
  }

  private static Action forMultiEntities(ActionType actionType, Class<? extends DomainEntity> type) {
    return new Action(actionType, type);
  }

}
