package nl.knaw.huygens.timbuctoo.persistence.persister;

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

import nl.knaw.huygens.persistence.PersistenceException;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceWrapper;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ModPersisterTest {

  public static final ProjectADomainEntity DOMAIN_ENTITY = new ProjectADomainEntity();
  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final int A_MILLISECOND = 1;

  private PersistenceWrapper persistenceWrapper;
  private ModPersister instance;

  @Before
  public void setUp() throws Exception {
    persistenceWrapper = mock(PersistenceWrapper.class);
    instance = new ModPersister(persistenceWrapper, A_MILLISECOND);
  }

  @Test
  public void executeUpdatesThePIDWithTheNewReferenceOfTheDomainEntity() throws Exception {
    // action
    instance.execute(DOMAIN_ENTITY);

    // verify
    verify(persistenceWrapper, times(1)).updatePID(DOMAIN_ENTITY);
  }

  @Test
  public void executeTriesFiveTimeAtMostWhenThePIDCannotBeUpdated() throws Exception {
    // setup
    doThrow(PersistenceException.class).when(persistenceWrapper).updatePID(DOMAIN_ENTITY);

    // action
    instance.execute(DOMAIN_ENTITY);

    // verify
    verify(persistenceWrapper, times(5)).updatePID(DOMAIN_ENTITY);
  }
}
