package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;

public class GraphWrapperFactoryTest {
  private GraphWrapperFactory instance;

  @Before
  public void setup() {
    instance = new GraphWrapperFactory();
  }

  @Test
  public void wrapCreatesANonTransactionGraphWrapperIfTheGraphIsNotATransactionalGraph() {
    // action
    GraphWrapper graphWrapper = instance.wrap(mock(Graph.class));

    // verify
    assertThat(graphWrapper, is(instanceOf(NonTransactionalGraphWrapper.class)));
  }

  @Test
  public void wrapCreatesATransactionGraphWrapperIfTheGraphIsATransactionGraph() {
    // action
    GraphWrapper graphWrapper = instance.wrap(mock(TransactionalGraph.class));

    // verify
    assertThat(graphWrapper, is(instanceOf(TransactionalGraphWrapper.class)));
  }

}
