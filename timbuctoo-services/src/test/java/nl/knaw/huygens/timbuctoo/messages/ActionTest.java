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
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_ACTION;
import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_ENTITY_ID;
import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_ENTITY_TYPE;
import static nl.knaw.huygens.timbuctoo.messages.Broker.PROP_FOR_MULTI_ENTITIES;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ActionTest {

  public static final String ID = "id";
  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final ActionType MOD = ActionType.MOD;
  private Session session;

  @Before
  public void setup() throws JMSException {
    Message message = mock(Message.class);
    session = mock(Session.class);
    when(session.createMessage()).thenReturn(message);
  }

  @Test
  public void createMessageCreatesANewMessageWithWithTheSettingsOfTheAction() throws JMSException {
    // setup
    Action action = new Action(MOD, TYPE, ID);

    // action
    Message message = action.createMessage(session);

    // verify
    verify(message).setStringProperty(PROP_ACTION, MOD.getStringRepresentation());
    verify(message).setStringProperty(PROP_ENTITY_TYPE, TypeNames.getInternalName(TYPE));
    verify(message).setStringProperty(PROP_ENTITY_ID, ID);
    verify(message).setBooleanProperty(PROP_FOR_MULTI_ENTITIES, false);
  }


  @Test
  public void createMessageDoesNotAddTheIdWhenItIsAMultipleUpdateAction() throws JMSException {
    // setup
    Action action = Action.multiUpdateActionFor(TYPE);

    // action
    Message message = action.createMessage(session);

    // verify
    verify(message).setStringProperty(PROP_ACTION, MOD.getStringRepresentation());
    verify(message).setStringProperty(PROP_ENTITY_TYPE, TypeNames.getInternalName(TYPE));
    verify(message).setBooleanProperty(PROP_FOR_MULTI_ENTITIES, true);
    verifyNoMoreInteractions(message);
  }
}
