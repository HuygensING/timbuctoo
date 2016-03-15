package nl.knaw.huygens.timbuctoo.index;

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

import nl.knaw.huygens.timbuctoo.index.request.IndexRequest;
import nl.knaw.huygens.timbuctoo.index.request.IndexRequestFactory;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IndexServiceTest {

  public static final ActionType ACTION_TYPE = ActionType.MOD;
  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ENTITY_ID = "entityId";
  public static final Action ACTION = new Action(ACTION_TYPE, TYPE, ENTITY_ID);
  public static final int A_MILLISECOND = 1;
  private IndexService instance;
  private IndexRequestFactory indexRequestFactory;
  private IndexRequest indexRequest;


  @Before
  public void setUp() throws Exception {
    setupIndexRequestFactory();
    instance = new IndexService(mock(Broker.class), indexRequestFactory, A_MILLISECOND);
  }

  private void setupIndexRequestFactory() {
    indexRequestFactory = mock(IndexRequestFactory.class);
    indexRequest = mock(IndexRequest.class);
    when(indexRequestFactory.forAction(any(Action.class))).thenReturn(indexRequest);
  }

  @Test
  public void executeActionForActionForSingleItemLetsTheIndexerExecuteACreateIndexRequestForTheAction() throws Exception {
    // action
    instance.executeAction(ACTION);

    // verify
    InOrder inOrder = inOrder(indexRequestFactory, indexRequest);
    inOrder.verify(indexRequestFactory).forAction(ACTION);
    inOrder.verify(indexRequest).execute();
  }
  
  @Test
  public void executeActionStopsRetryingAfterFiveTimesWhenAnIndexExceptionIsThrown() throws Exception {
    // setup
    doThrow(IndexException.class).when(indexRequest).execute();

    // action
    instance.executeAction(ACTION);

    // verify
    verify(indexRequest, times(5)).execute();
  }

  @Test
  public void executeActionStopsRetryingAfterFiveTimesWhenARuntimeExceptionIsThjrow() throws Exception {
    // setup
    doThrow(RuntimeException.class).when(indexRequest).execute();

    // action
    instance.executeAction(ACTION);

    // verify
    verify(indexRequest, times(5)).execute();
  }
}
