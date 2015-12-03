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

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.indexer.IndexerFactory;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequestFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.messages.Producer;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.persister.PersisterFactory;
import nl.knaw.huygens.timbuctoo.persistence.request.PersistenceRequestFactory;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import javax.jms.JMSException;

import static nl.knaw.huygens.timbuctoo.messages.ActionType.ADD;
import static nl.knaw.huygens.timbuctoo.messages.ActionType.DEL;
import static nl.knaw.huygens.timbuctoo.messages.ActionType.END;
import static nl.knaw.huygens.timbuctoo.messages.ActionType.MOD;
import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


public class ChangeHelperTest {

  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ID = "id";
  public static final ProjectADomainEntity DOMAIN_ENTITY = new ProjectADomainEntity();
  private Producer persistenceProducer;
  private ChangeHelper instance;
  private Broker broker;
  private Producer indexProducer;

  @Before
  public void setUp() throws Exception {
    setupBroker();
    instance = new ChangeHelper(broker, new PersistenceRequestFactory(mock(Repository.class), mock(PersisterFactory.class)), new IndexRequestFactory(mock(IndexerFactory.class), mock(Repository.class), mock(TypeRegistry.class)));
  }

  private void setupBroker() throws JMSException {
    persistenceProducer = mock(Producer.class);
    broker = mock(Broker.class);
    when(broker.getProducer(ChangeHelper.PERSIST_MSG_PRODUCER, Broker.PERSIST_QUEUE)).thenReturn(persistenceProducer);
    indexProducer = mock(Producer.class);
    when(broker.getProducer(ChangeHelper.INDEX_MSG_PRODUCER, Broker.INDEX_QUEUE)).thenReturn(indexProducer);
  }

  @Test
  public void sendPersistMessageSendsAnActionCreatedFromTheParametersOfTheMethod() throws JMSException {
    // action
    PersistenceRequest persistenceRequest = mock(PersistenceRequest.class);
    Action action = new Action();
    when(persistenceRequest.toAction()).thenReturn(action);
    instance.sendPersistMessage(persistenceRequest);

    // verify
    verify(persistenceProducer).send(action);
  }

  @Test
  public void notifyChangeWithActionTypeADDSendsAnAddActionToTheIndexAndThePersistenceQueue() throws JMSException {
    // action
    instance.notifyChange(ADD, TYPE, ID);

    // verify
    verify(indexProducer).send(argThat(likeAction() //
      .withActionType(ADD) //
      .withType(TYPE) //
      .withId(ID)));

    verify(persistenceProducer).send(argThat(likeAction() //
      .withActionType(ADD) //
      .withType(TYPE) //
      .withId(ID)));

  }

  @Test
  public void notifyChangeWithActionTypeMODSendsAModificationActionToTheIndexAndAnAddActionThePersistenceQueue() throws JMSException {
    // action
    instance.notifyChange(MOD, TYPE, ID);

    // verify
    // verify
    verify(indexProducer).send(argThat(likeAction() //
      .withActionType(MOD) //
      .withType(TYPE) //
      .withId(ID)));

    verify(persistenceProducer).send(argThat(likeAction() //
      .withActionType(ADD) //
      .withType(TYPE) //
      .withId(ID)));

  }

  @Test
  public void notifyChangeWithActionTypeDELSendsADeleteActionToTheIndexQueue() throws JMSException {
    // action
    instance.notifyChange(DEL, TYPE, ID);

    // verify
    verify(indexProducer).send(argThat(likeAction() //
      .withActionType(DEL) //
      .withType(TYPE) //
      .withId(ID)));

    // if we delete a variant of an entity other VRE's still have to be able to use the entity.
    verify(persistenceProducer).send(argThat(likeAction() //
      .withActionType(ADD) //
      .withType(TYPE) //
      .withId(ID)));
  }

  @Test
  public void notifyChangeWithActionTypeENDDoesNotSendsActions() {
    // action
    instance.notifyChange(END, TYPE, ID);

    // verify
    verifyZeroInteractions(indexProducer, persistenceProducer);
  }

}
