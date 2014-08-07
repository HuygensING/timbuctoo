package nl.knaw.huygens.timbuctoo.persistence;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.Before;
import org.junit.Test;

import test.rest.model.projecta.ProjectADomainEntity;

public class PersistenceServiceTest {

  private static final int DEFAULT_REVISION = 2;
  private static final Class<ProjectADomainEntity> DEFAULT_TYPE = ProjectADomainEntity.class;
  private static final String DEFAULT_ID = "PADE00000000001";
  public static final String DEFAULT_PID = "1234567-sdfya378t14-231423746123-sadfasf";
  public static final String DEFAULT_PID_URL = "http://test.test.org/prefix/" + DEFAULT_PID;

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

    when(repository.getEntity(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);

    instance = new PersistenceService(broker, persistenceWrapper, repository);
  }

  @Test
  public void testExecuteActionADD() throws Exception {
    testExecute(ActionType.ADD);

    verify(repository).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(persistenceWrapper).getPersistentURL(DEFAULT_PID);
    verify(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
  }

  @Test
  public void testExecuteActionADDAlreadyHasAPID() throws Exception {
    doThrow(IllegalStateException.class).when(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    testExecute(ActionType.ADD);

    verify(repository).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(persistenceWrapper).getPersistentURL(DEFAULT_PID);
    verify(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    verify(persistenceWrapper).deletePersistentId(DEFAULT_PID);
  }

  @Test
  public void testExecuteActionADDCreationOfThePIDWentWrong() throws Exception {
    doThrow(PersistenceException.class).when(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);

    testExecute(ActionType.ADD);

    verify(repository, only()).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper, only()).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
  }

  @Test
  public void testExecuteActionADDSettingPIDWentWrong() throws Exception {
    doThrow(StorageException.class).when(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    testExecute(ActionType.ADD);

    verify(repository).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(persistenceWrapper).getPersistentURL(DEFAULT_PID);
    verify(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    verify(persistenceWrapper).deletePersistentId(DEFAULT_PID);
  }

  @Test
  public void testExecuteActionMOD() throws Exception {
    testExecute(ActionType.MOD);

    verify(repository).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(persistenceWrapper).getPersistentURL(DEFAULT_PID);
    verify(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
  }

  @Test
  public void testExecuteActionMODAlreadyHasAPID() throws Exception {
    doThrow(IllegalStateException.class).when(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    testExecute(ActionType.MOD);

    verify(repository).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    verify(persistenceWrapper).deletePersistentId(DEFAULT_PID);
  }

  @Test
  public void testExecuteActionMODCreationOfThePIDWentWrong() throws Exception {
    doThrow(PersistenceException.class).when(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);

    testExecute(ActionType.MOD);

    verify(repository, only()).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper, only()).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
  }

  @Test
  public void testExecuteActionMODSettingPIDWentWrong() throws Exception {
    doThrow(StorageException.class).when(repository).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID_URL);
    testExecute(ActionType.MOD);

    verify(repository).getEntity(DEFAULT_TYPE, DEFAULT_ID);
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

  private void testExecute(ActionType actionType) throws Exception {
    Action action = new Action(actionType, DEFAULT_TYPE, DEFAULT_ID);

    instance.executeAction(action);
  }

}
