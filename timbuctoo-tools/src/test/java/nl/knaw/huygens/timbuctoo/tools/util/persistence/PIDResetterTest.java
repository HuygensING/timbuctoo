package nl.knaw.huygens.timbuctoo.tools.util.persistence;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import test.tools.model.projecta.ProjectADomainEntity;

import com.google.common.collect.Lists;

public class PIDResetterTest {
  private static final String PID = "pid";
  private static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  private Repository repositoryMock;
  private PersistenceWrapper persistenceWrapperMock;
  private PIDResetter pidResetter;

  @Before
  public void setUp() {
    persistenceWrapperMock = mock(PersistenceWrapper.class);
    repositoryMock = mock(Repository.class);

    pidResetter = new PIDResetter(repositoryMock, persistenceWrapperMock);
  }

  @Test
  public void resetsThePIDOfEveryVersionOfTheEntity() throws PersistenceException {
    // setup
    setupRepository(2, 2, 2);

    // action
    pidResetter.resetPIDsFor(TYPE);

    //verify
    verify(persistenceWrapperMock, times(4)).updatePID(anyString(), Matchers.<Class<? extends Entity>> any(), anyString(), anyInt());
  }

  private void setupRepository(int numberOfEntities, int numberOfRevisions, int numberOfRevisionsWithPID) {
    List<ProjectADomainEntity> entities = Lists.newArrayList();

    for (int i = 0; i < numberOfEntities; i++) {
      String id = "id" + i;
      entities.add(new ProjectADomainEntity(id));

      List<ProjectADomainEntity> revisions = Lists.newArrayList();
      for (int j = 0; j < numberOfRevisions; j++) {
        ProjectADomainEntity entity = new ProjectADomainEntity(id);
        entity.setRev(j);
        if (j < numberOfRevisionsWithPID) {
          entity.setPid(PID);
        }
        revisions.add(entity);

        when(repositoryMock.getVersions(TYPE, id)).thenReturn(revisions);
      }
    }

    when(repositoryMock.getDomainEntities(TYPE)).thenReturn(StorageIteratorStub.newInstance(entities));

  }

  @Test
  public void ignoresRevisionsWithoutPID() throws PersistenceException {
    setupRepository(2, 2, 1);

    // action
    pidResetter.resetPIDsFor(TYPE);

    // verify
    verify(persistenceWrapperMock, times(2)).updatePID(anyString(), Matchers.<Class<? extends Entity>> any(), anyString(), anyInt());
  }

  @Test
  public void continuesWhenThePersistenceWrapperThrowsAnException() throws PersistenceException {
    // setup
    setupRepository(2, 2, 2);

    persistenceManagerThrowsExceptionWithFirstCall();

    // action
    pidResetter.resetPIDsFor(TYPE);

    //verify
    verify(persistenceWrapperMock, times(4)).updatePID(anyString(), Matchers.<Class<? extends Entity>> any(), anyString(), anyInt());
  }

  private void persistenceManagerThrowsExceptionWithFirstCall() throws PersistenceException {
    doThrow(PersistenceException.class).doNothing().when(persistenceWrapperMock).updatePID(anyString(), Matchers.<Class<? extends Entity>> any(), anyString(), anyInt());
  }
}
