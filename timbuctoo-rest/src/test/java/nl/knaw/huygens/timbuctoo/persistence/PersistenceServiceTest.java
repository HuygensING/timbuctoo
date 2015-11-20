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

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.persistence.request.PersistenceRequestFactory;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PersistenceServiceTest {

  private static final int DEFAULT_REVISION = 2;
  private static final Class<ProjectADomainEntity> DEFAULT_TYPE = ProjectADomainEntity.class;
  private static final String DEFAULT_ID = "PADE00000000001";
  public static final String DEFAULT_PID = "1234567-sdfya378t14-231423746123-sadfasf";
  public static final String DEFAULT_PID_URL = "http://test.test.org/prefix/" + DEFAULT_PID;

  private PersistenceService instance;
  private PersistenceWrapper persistenceWrapper;
  private Repository repository;
  private Action action;
  private PersistenceRequest persistenceRequest;
  private PersistenceRequestFactory persistenceRequestFactory;
  private PersisterFactory persisterFactory;

  @Before
  public void setUp() throws Exception {
    Broker broker = mock(Broker.class);

    persistenceWrapper = mock(PersistenceWrapper.class);
    when(persistenceWrapper.persistObject(DEFAULT_TYPE, DEFAULT_ID, DEFAULT_REVISION)).thenReturn(DEFAULT_PID);
    when(persistenceWrapper.getPersistentURL(anyString())).thenReturn(DEFAULT_PID_URL);

    repository = mock(Repository.class);

    action = mock(Action.class);
    persistenceRequest = mock(PersistenceRequest.class);
    persisterFactory = mock(PersisterFactory.class);
    setupPersistenceRequestFactory();
    instance = new PersistenceService(broker, persistenceRequestFactory, persisterFactory);
  }

  private void setupPersistenceRequestFactory() {
    persistenceRequestFactory = mock(PersistenceRequestFactory.class);
    when(persistenceRequestFactory.forAction(action)).thenReturn(persistenceRequest);
  }

  @Test
  public void executeCreatesAnIndexRequestFromTheActionAndExecutesIt(){
    // setup

    // action
    instance.executeAction(action);

    // verify
    verify(persistenceRequest).execute(persisterFactory);
  }
}
