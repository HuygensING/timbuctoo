package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

/*
 * #%L
 * Timbuctoo core
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

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class NonTransactionalGraphTest extends AbstractGraphWrapperTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private Graph graph;
  private NonTransactionalGraph instance;

  @Before
  public void setup() {
    graph = mock(Graph.class);
    instance = new NonTransactionalGraph(graph);
  }

  @Test
  public void stopTransactionThrowsAnUnsupportedOperationException() {
    // setup
    expectedException.expect(UnsupportedOperationException.class);
    expectedException.expectMessage(NonTransactionalGraph.STOP_TRANSACTION_EXCEPTION_MESSAGE);

    // action
    instance.stopTransaction(Conclusion.SUCCESS);
  }

  @Test
  public void commitDoesNothing(){
    // action
    instance.commit();

    // verify
    verifyZeroInteractions(graph);
  }

  @Test
  public void rollBackDoesNothing(){
    // action
    instance.rollback();

    //  verify
    verifyZeroInteractions(graph);
  }


  @Override
  protected AbstractGraphWrapper getInstance() {
    return this.instance;
  }

  @Override
  protected Graph getDelegate() {
    return this.graph;
  }
}
