package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

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
