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

import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.persistence.request.PersistenceRequestFactory;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PersistenceServiceTest {
  private PersistenceService instance;
  private Action action;
  private PersistenceRequest persistenceRequest;
  private PersistenceRequestFactory persistenceRequestFactory;

  @Before
  public void setUp() throws Exception {
    Broker broker = mock(Broker.class);

    action = mock(Action.class);
    persistenceRequest = mock(PersistenceRequest.class);
    setupPersistenceRequestFactory();
    instance = new PersistenceService(broker, persistenceRequestFactory);
  }

  private void setupPersistenceRequestFactory() {
    persistenceRequestFactory = mock(PersistenceRequestFactory.class);
    when(persistenceRequestFactory.forAction(action)).thenReturn(persistenceRequest);
  }

  @Test
  public void executeCreatesAnIndexRequestFromTheActionAndExecutesIt(){
    // action
    instance.executeAction(action);

    // verify
    verify(persistenceRequest).execute();
  }
}
