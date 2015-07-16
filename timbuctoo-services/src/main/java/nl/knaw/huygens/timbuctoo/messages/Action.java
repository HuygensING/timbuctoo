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
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_ACTION;
import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_DOC_ID;
import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_DOC_TYPE;
import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_IS_MULTI_ENTITY;

public class Action {

  private final ActionType actionType;
  private String id;
  private final Class<? extends DomainEntity> type;
  private final boolean isMultiEntity;

  public Action(ActionType actionType, Class<? extends DomainEntity> type, String id) {
    this.actionType = actionType;
    this.id = id;
    this.type = type;
    this.isMultiEntity = false;
  }

  private Action(ActionType actionType, Class<? extends DomainEntity> type) {
    this.actionType = actionType;
    this.type = type;
    this.isMultiEntity = true;
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
    message.setStringProperty(PROP_DOC_TYPE, TypeNames.getInternalName(type));
    message.setBooleanProperty(PROP_IS_MULTI_ENTITY, isMultiEntity);
    if (!isMultiEntity) {
      message.setStringProperty(PROP_DOC_ID, id);
    }

    return message;
  }
}
