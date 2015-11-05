package nl.knaw.huygens.timbuctoo.persistence;

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

import com.google.common.collect.Lists;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static nl.knaw.huygens.timbuctoo.DomainEntityMatcher.likeDomainEntity;
import static nl.knaw.huygens.timbuctoo.ProjectADomainEntityBuilder.aDomainEntity;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class PersistenceServiceTest {

  private static final int DEFAULT_REVISION = 2;
  private static final Class<ProjectADomainEntity> DEFAULT_TYPE = ProjectADomainEntity.class;
  private static final String DEFAULT_ID = "PADE00000000001";
  public static final String DEFAULT_PID = "1234567-sdfya378t14-231423746123-sadfasf";
  public static final String DEFAULT_PID_URL = "http://test.test.org/prefix/" + DEFAULT_PID;
  public static final String ID_1 = "id1";
  public static final String PID_1 = "pid1";
  public static final int REV_1 = 1;
  public static final int REV_2 = 2;
  public static final String PID_2 = "pid2";
  public static final String ID_2 = "id2";
  public static final String PID_3 = "pid3";
  public static final String PID_4 = "pid4";
  public static final String ID_3 = "id3";

  private PersistenceService instance;
  private PersistenceWrapper persistenceWrapper;
  private Repository repository;

  @Before
  public void setUp() throws Exception {
    Broker broker = mock(Broker.class);

    persistenceWrapper = mock(PersistenceWrapper.class);
    when(persistenceWrapper.persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION)).thenReturn(DEFAULT_PID);
    when(persistenceWrapper.getPersistentURL(anyString())).thenReturn(DEFAULT_PID_URL);

    repository = mock(Repository.class);

    ProjectADomainEntity entity = new ProjectADomainEntity();
    entity.setId(DEFAULT_ID);
    entity.setRev(DEFAULT_REVISION);

    when(repository.getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);

    instance = new PersistenceService(broker, persistenceWrapper, repository);
  }

  @Test
  public void testExecuteActionADD() throws Exception {
    testExecute(ActionType.ADD);

    verify(repository).getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(persistenceWrapper).getPersistentURL(DEFAULT_PID);
    verify(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
  }

  @Test
  public void testExecuteActionADDAlreadyHasAPID() throws Exception {
    doThrow(IllegalStateException.class).when(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    testExecute(ActionType.ADD);

    verify(repository).getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(persistenceWrapper).getPersistentURL(DEFAULT_PID);
    verify(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    verify(persistenceWrapper).deletePersistentId(DEFAULT_PID);
  }

  @Test
  public void testExecuteActionADDCreationOfThePIDWentWrong() throws Exception {
    doThrow(PersistenceException.class).when(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);

    testExecute(ActionType.ADD);

    verify(repository, only()).getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper, only()).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
  }

  @Test
  public void testExecuteActionADDSettingPIDWentWrong() throws Exception {
    doThrow(StorageException.class).when(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    testExecute(ActionType.ADD);

    verify(repository).getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(persistenceWrapper).getPersistentURL(DEFAULT_PID);
    verify(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    verify(persistenceWrapper).deletePersistentId(DEFAULT_PID);
  }

  @Test
  public void testExecuteActionMOD() throws Exception {
    testExecute(ActionType.MOD);

    verify(repository).getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(persistenceWrapper).getPersistentURL(DEFAULT_PID);
    verify(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
  }

  @Test
  public void testExecuteActionMODAlreadyHasAPID() throws Exception {
    doThrow(IllegalStateException.class).when(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    testExecute(ActionType.MOD);

    verify(repository).getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    verify(persistenceWrapper).deletePersistentId(DEFAULT_PID);
  }

  @Test
  public void testExecuteActionMODCreationOfThePIDWentWrong() throws Exception {
    doThrow(PersistenceException.class).when(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);

    testExecute(ActionType.MOD);

    verify(repository, only()).getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper, only()).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
  }

  @Test
  public void testExecuteActionMODSettingPIDWentWrong() throws Exception {
    doThrow(StorageException.class).when(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    testExecute(ActionType.MOD);

    verify(repository).getEntityOrDefaultVariation(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    verify(persistenceWrapper).deletePersistentId(DEFAULT_PID);
  }

  @Test
  public void testExecuteActionDEL() throws Exception {
    testExecute(ActionType.DEL);

    verifyZeroInteractions(repository, persistenceWrapper);
  }

  @Test
  public void testExecuteActionDefault() throws Exception {
    testExecute(ActionType.END);

    verifyZeroInteractions(repository, persistenceWrapper);
  }

  @Test
  public void executeWithModForMultiEntitiesUpdatesThePIDOfAllTheVersionsOfAllTheEntitiesOfACertainTypeIfTheyHaveAPID() throws StorageException, PersistenceException {
    // setup
    ProjectADomainEntity entity1Rev1 = aDomainEntity().withId(ID_1).withRev(REV_1).withPID(PID_1).build();
    ProjectADomainEntity entity1Rev2 = aDomainEntity().withId(ID_1).withRev(REV_2).withPID(PID_2).build();

    ProjectADomainEntity entity2Rev1 = aDomainEntity().withId(ID_2).withRev(REV_1).withPID(PID_3).build();
    ProjectADomainEntity entity2Rev2 = aDomainEntity().withId(ID_2).withRev(REV_2).withPID(PID_4).build();

    ProjectADomainEntity entityWithoutPID = aDomainEntity().withId(ID_3).withRev(1).build();

    repositoryRetrievesEntities(entity1Rev1, entity1Rev2, entity2Rev1, entity2Rev2, entityWithoutPID);


    Action action = Action.multiUpdateActionFor(DEFAULT_TYPE);

    // action
    instance.executeAction(action);

    // verify
    verify(persistenceWrapper).updatePID(argThat(
        likeDomainEntity().ofType(DEFAULT_TYPE)//
            .withId(ID_1) //
            .withPID(PID_1) //
            .withRevision(REV_1)));
    verify(persistenceWrapper).updatePID(argThat(
        likeDomainEntity().ofType(DEFAULT_TYPE)//
            .withId(ID_1) //
            .withPID(PID_2) //
            .withRevision(REV_2)));

    verify(persistenceWrapper).updatePID(argThat(
        likeDomainEntity().ofType(DEFAULT_TYPE)//
            .withId(ID_2) //
            .withPID(PID_3) //
            .withRevision(REV_1)));
    verify(persistenceWrapper).updatePID(argThat(
        likeDomainEntity().ofType(DEFAULT_TYPE)//
            .withId(ID_2) //
            .withPID(PID_4) //
            .withRevision(REV_2)));
    verify(persistenceWrapper, never()).updatePID(argThat(
        likeDomainEntity().ofType(DEFAULT_TYPE)//
            .withId(ID_3)));
  }

  private void repositoryRetrievesEntities(ProjectADomainEntity entity1Rev1, ProjectADomainEntity entity1Rev2, ProjectADomainEntity entity2Rev1, ProjectADomainEntity entity2Rev2, ProjectADomainEntity entityWithoutPID
  ) throws StorageException {
    StorageIteratorStub<ProjectADomainEntity> iterator = StorageIteratorStub.newInstance(Lists.newArrayList(entity1Rev2, entity2Rev2, entityWithoutPID));
    when(repository.getDomainEntities(DEFAULT_TYPE)).thenReturn(iterator);

    when(repository.getAllRevisions(DEFAULT_TYPE, entity1Rev2.getId())).thenReturn(Lists.newArrayList(entity1Rev1, entity1Rev2));
    when(repository.getAllRevisions(DEFAULT_TYPE, entity2Rev2.getId())).thenReturn(Lists.newArrayList(entity2Rev1, entity2Rev2));
  }

  private void testExecute(ActionType actionType) throws Exception {
    Action action = new Action(actionType, DEFAULT_TYPE, DEFAULT_ID);

    instance.executeAction(action);
  }

}
