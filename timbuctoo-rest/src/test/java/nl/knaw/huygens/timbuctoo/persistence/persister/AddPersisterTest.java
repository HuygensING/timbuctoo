package nl.knaw.huygens.timbuctoo.persistence.persister;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.AlreadyHasAPidException;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import test.rest.model.projecta.ProjectADomainEntity;

import static nl.knaw.huygens.timbuctoo.persistence.persister.AddPersister.DOMAIN_ENTITY_CANNOT_BE_NULL_MESSAGE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class AddPersisterTest {
  public static final ProjectADomainEntity DOMAIN_ENTITY = new ProjectADomainEntity();
  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ID = "id";
  public static final int REVISION = 1;
  public static final String PID = "pid";

  static {
    DOMAIN_ENTITY.setId(ID);
    DOMAIN_ENTITY.setRev(REVISION);
  }

  private PersistenceWrapper persistenceWrapper;
  private Repository repository;
  private AddPersister instance;

  @Before
  public void setUp() throws Exception {
    persistenceWrapper = mock(PersistenceWrapper.class);
    repository = mock(Repository.class);
    instance = new AddPersister(repository, persistenceWrapper);
  }

  @Test
  public void executeLetsThePersistenceWrapperCreateAPIDForTheEntityAndSetsThePIDForTheEntity() throws Exception {
    // setup
    when(persistenceWrapper.persistObject(any(), anyString(), anyInt())).thenReturn(PID);

    // action
    instance.execute(DOMAIN_ENTITY);

    // verify
    verify(persistenceWrapper, times(1)).persistObject(TYPE, ID, REVISION);
    verify(repository, times(1)).setPID(TYPE, ID, PID);
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void executeThrowsAnIllegalArgumentExceptionWhenTheDomainEntityIsNull() {
    // setup expectedException
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(DOMAIN_ENTITY_CANNOT_BE_NULL_MESSAGE);

    // action
    instance.execute(null);

    verifyZeroInteractions(persistenceWrapper, repository);
  }

  @Test
  public void executeTriesToPersistAtMostFiveTimesWhenAPersistenceExceptionIsThrown() throws PersistenceException {
    // setup
    when(persistenceWrapper.persistObject(any(), anyString(), anyInt())).thenThrow(new PersistenceException(""));

    // action
    instance.execute(DOMAIN_ENTITY);

    // verify
    verify(persistenceWrapper, times(5)).persistObject(TYPE, ID, REVISION);
    verifyZeroInteractions(repository);
  }

  @Test
  public void executeTriesToStoreAtMostFiveTimeWhenAnExceptionIsThrownThenDeletesThePID() throws Exception {
    // setup
    when(persistenceWrapper.persistObject(any(), anyString(), anyInt())).thenReturn(PID);
    doThrow(Exception.class).when(repository).setPID(any(), anyString(), anyString());

    // action
    instance.execute(DOMAIN_ENTITY);

    // verify
    verify(repository, times(5)).setPID(TYPE, ID, PID);
    verify(persistenceWrapper).deletePersistentId(PID);
  }

  @Test
  public void executeTriesToStoreAtMostFiveTimeWhenARuntimeExceptionIsThrownByTheStorageThenDeletesThePID() throws Exception {
    // setup
    when(persistenceWrapper.persistObject(any(), anyString(), anyInt())).thenReturn(PID);
    doThrow(RuntimeException.class).when(repository).setPID(any(), anyString(), anyString());

    // action
    instance.execute(DOMAIN_ENTITY);

    // verify
    verify(repository, times(5)).setPID(TYPE, ID, PID);
    verify(persistenceWrapper).deletePersistentId(PID);
  }

  @Test
  public void executeRemovesThePidWithoutRetryingWhenStorageThrowsAnEntityAlreadyHasAPidException() throws Exception{
    // setup
    when(persistenceWrapper.persistObject(any(), anyString(), anyInt())).thenReturn(PID);
    doThrow(AlreadyHasAPidException.class).when(repository).setPID(any(), anyString(), anyString());

    // action
    instance.execute(DOMAIN_ENTITY);

    // verify
    verify(repository, times(1)).setPID(TYPE, ID, PID);
    verify(persistenceWrapper).deletePersistentId(PID);
  }


}
