package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TransactionalGraphFactoryWrapperTest {
  @Test
  public void addTransactionalGraphAddsTheGraphIfItIsATransactionGraph() {
    // setup
    TransactionalGraph graph = mock(TransactionalGraph.class);
    CompositeGraphWrapper graphWrapper = mock(CompositeGraphWrapper.class);

    TransactionalGraphWrapperFactory instance = new TransactionalGraphWrapperFactory();

    // action
    instance.addTransactionalGraph(graphWrapper, graph);

    // verify
    verify(graphWrapper).setTranactionalGraph(graph);
  }

  @Test
  public void addTransactionalGraphAddsANoOpTransactionalGraphWrapperIfTheGraphIsNotATransactionalGraph() {
    // setup
    Graph graph = mock(Graph.class);
    CompositeGraphWrapper graphWrapper = mock(CompositeGraphWrapper.class);

    TransactionalGraphWrapperFactory instance = new TransactionalGraphWrapperFactory();

    // action
    instance.addTransactionalGraph(graphWrapper, graph);

    // verify
    verify(graphWrapper).setTranactionalGraph(any(NoOpTransactionalGraphWrapper.class));
  }

}
