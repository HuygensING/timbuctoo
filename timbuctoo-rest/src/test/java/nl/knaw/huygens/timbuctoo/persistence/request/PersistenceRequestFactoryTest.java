package nl.knaw.huygens.timbuctoo.persistence.request;

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
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.persister.PersisterFactory;
import org.junit.Before;
import org.junit.Test;
import test.model.projecta.ProjectAPerson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class PersistenceRequestFactoryTest {

  public static final ActionType ACTION_TYPE = ActionType.ADD;
  public static final Class<ProjectAPerson> TYPE = ProjectAPerson.class;
  public static final String ID = "id";
  private PersistenceRequestFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new PersistenceRequestFactory(mock(Repository.class), mock(PersisterFactory.class));
  }

  @Test
  public void forEntityCreatesAnEntityPersistenceRequest() throws Exception {
    // action
    PersistenceRequest persistenceRequest = instance.forEntity(ACTION_TYPE, TYPE, ID);

    // verify
    assertThat(persistenceRequest, is(instanceOf(EntityPersistenceRequest.class)));
  }

  @Test
  public void forCollectionCreatesACollectionPersistenceRequest(){
    // action
    PersistenceRequest persistenceRequest = instance.forCollection(ACTION_TYPE, TYPE);

    // verify
    assertThat(persistenceRequest, is(instanceOf(CollectionPersistenceRequest.class)));
  }

  @Test
  public void forActionCreatesAnEntityPersistenceRequestIfTheActionIsForASingleEntity(){
    // setup
    Action actionForSingleEntity = new Action(ACTION_TYPE, TYPE, ID);

    // action
    PersistenceRequest persistenceRequest = instance.forAction(actionForSingleEntity);

    // verify
    assertThat(persistenceRequest, is(instanceOf(EntityPersistenceRequest.class)));
  }

  @Test
  public void forActionCreatesACollectionPersistenceRequestIfTheActionIsForMultipleEntities(){
    // setup
    Action actionForMultipleEntities = new Action(ACTION_TYPE, TYPE);

    // action
    PersistenceRequest persistenceRequest = instance.forAction(actionForMultipleEntities);

    // verify
    assertThat(persistenceRequest, is(instanceOf(CollectionPersistenceRequest.class)));
  }
}
