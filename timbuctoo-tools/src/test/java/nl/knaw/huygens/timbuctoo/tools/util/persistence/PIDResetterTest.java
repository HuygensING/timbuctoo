package nl.knaw.huygens.timbuctoo.tools.util.persistence;

/*
 * #%L
 * Timbuctoo tools
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
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import org.junit.Before;
import org.junit.Test;
import test.tools.model.projecta.ProjectADomainEntity;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    verify(persistenceWrapperMock, times(4)).updatePID(any(TYPE));
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
    verify(persistenceWrapperMock, times(2)).updatePID(any(TYPE));
  }

  @Test
  public void continuesWhenThePersistenceWrapperThrowsAnException() throws PersistenceException {
    // setup
    setupRepository(2, 2, 2);

    persistenceManagerThrowsExceptionWithFirstCall();

    // action
    pidResetter.resetPIDsFor(TYPE);

    //verify
    verify(persistenceWrapperMock, times(4)).updatePID(any(TYPE));
  }



  private void persistenceManagerThrowsExceptionWithFirstCall() throws PersistenceException {
    doThrow(PersistenceException.class).doNothing().when(persistenceWrapperMock).updatePID(any(TYPE));
  }
}
