package nl.knaw.huygens.timbuctoo.persistence;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.jms.JMSException;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.rest.model.projecta.ProjectADomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;

import org.junit.Before;
import org.junit.Test;

public class PersistenceServiceTest {

  private static final int DEFAULT_REVISION = 2;
  private static final Class<ProjectADomainEntity> DEFAULT_TYPE = ProjectADomainEntity.class;
  private static final String DEFAULT_ID = "PADE00000000001";
  public static final String DEFAULT_PID = "1234567-sdfya378t14-231423746123-sadfasf";
  private PersistenceService instance;
  private PersistenceWrapper persistenceWrapper;
  private StorageManager storageManager;

  @Before
  public void setUp() throws JMSException, PersistenceException {
    Broker broker = mock(Broker.class);

    persistenceWrapper = mock(PersistenceWrapper.class);
    when(persistenceWrapper.persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION)).thenReturn(DEFAULT_PID);

    storageManager = mock(StorageManager.class);

    ProjectADomainEntity entity = new ProjectADomainEntity();
    entity.setId(DEFAULT_ID);
    entity.setRev(DEFAULT_REVISION);

    when(storageManager.getEntity(DEFAULT_TYPE, DEFAULT_ID)).thenReturn(entity);

    instance = new PersistenceService(broker, persistenceWrapper, storageManager);
  }

  @Test
  public void testExecuteActionADD() throws JMSException, PersistenceException, IOException {
    testExecute(ActionType.ADD);

    verify(storageManager).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper, only()).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(storageManager).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID);
  }

  @Test
  public void testExecuteActionADDAlreadyHasAPID() throws JMSException, IOException, PersistenceException {
    doThrow(IllegalStateException.class).when(storageManager).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID);
    testExecute(ActionType.ADD);

    verify(storageManager).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(storageManager).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID);
    verify(persistenceWrapper).deletePersistentId(DEFAULT_PID);
  }

  @Test
  public void testExecuteActionADDCreationOfThePIDWentWrong() throws PersistenceException, JMSException {
    doThrow(PersistenceException.class).when(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);

    testExecute(ActionType.ADD);

    verify(storageManager, only()).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper, only()).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
  }

  @Test
  public void testExecuteActionADDSettingPIDWentWrong() throws IOException, JMSException, PersistenceException {
    doThrow(IOException.class).when(storageManager).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID);
    testExecute(ActionType.ADD);

    verify(storageManager).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(storageManager).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID);
    verify(persistenceWrapper).deletePersistentId(DEFAULT_PID);
  }

  @Test
  public void testExecuteActionMOD() throws JMSException, PersistenceException, IOException {
    testExecute(ActionType.MOD);

    verify(storageManager).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper, only()).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(storageManager).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID);
  }

  @Test
  public void testExecuteActionMODAlreadyHasAPID() throws IOException, JMSException, PersistenceException {
    doThrow(IllegalStateException.class).when(storageManager).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID);
    testExecute(ActionType.MOD);

    verify(storageManager).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(storageManager).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID);
    verify(persistenceWrapper).deletePersistentId(DEFAULT_PID);
  }

  @Test
  public void testExecuteActionMODCreationOfThePIDWentWrong() throws JMSException, PersistenceException {
    doThrow(PersistenceException.class).when(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);

    testExecute(ActionType.MOD);

    verify(storageManager, only()).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper, only()).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
  }

  @Test
  public void testExecuteActionMODSettingPIDWentWrong() throws IOException, JMSException, PersistenceException {
    doThrow(IOException.class).when(storageManager).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID);
    testExecute(ActionType.MOD);

    verify(storageManager).getEntity(DEFAULT_TYPE, DEFAULT_ID);
    verify(persistenceWrapper).persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION);
    verify(storageManager).setPID(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_PID);
    verify(persistenceWrapper).deletePersistentId(DEFAULT_PID);
  }

  @Test
  public void testExecuteActionDEL() throws JMSException {
    testExecute(ActionType.DEL);

    verifyZeroInteractions(storageManager, persistenceWrapper);
  }

  @Test
  public void testExecuteActionDefault() throws JMSException {
    testExecute(ActionType.END);

    verifyZeroInteractions(storageManager, persistenceWrapper);
  }

  private void testExecute(ActionType actionType) throws JMSException {
    Action action = new Action(actionType, DEFAULT_TYPE, DEFAULT_ID);

    instance.executeAction(action);
  }
}
