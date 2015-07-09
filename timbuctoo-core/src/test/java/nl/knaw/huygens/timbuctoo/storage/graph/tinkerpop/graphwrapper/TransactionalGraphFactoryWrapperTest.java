package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

public class TransactionalGraphFactoryWrapperTest {

  private TransactionalGraphWrapperFactory instance;

  @Before
  public void setup() {
    instance = new TransactionalGraphWrapperFactory();
  }

  @Test
  public void wrapReturnsTheGraphIfItIsATransactionalGraph() {
    // setup
    TransactionalGraph graph = mock(TransactionalGraph.class);

    // action
    TransactionalGraph returnedValue = instance.wrap(graph);

    // verify
    assertThat(returnedValue, is(sameInstance(graph)));
  }

  @Test
  public void wrapReturnsANoOpTransactionGraphWrapperIfItIsNotATransactionalGraph() {
    // setup
    Graph graph = mock(Graph.class);

    // action
    TransactionalGraph returnedValue = instance.wrap(graph);

    // verify
    assertThat(returnedValue, is(instanceOf(NoOpTransactionalGraphWrapper.class)));
  }

}
