package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KeyIndexableGraphWrapperFactoryTest {

  private CompositeGraphWrapper compositeGraphWrapper;
  private KeyIndexableGraphWrapperFactory instance;

  @Before
  public void setup() {
    compositeGraphWrapper = mock(CompositeGraphWrapper.class);

    instance = new KeyIndexableGraphWrapperFactory();
  }

  @Test
  public void addKeyIndexableGraphAddsTheGraphIfItIsAKeyIndexableGraph() {
    // setup
    KeyIndexableGraph graph = mock(KeyIndexableGraph.class);

    // action
    instance.addKeyIndexableGraph(compositeGraphWrapper, graph);

    // verify
    verify(compositeGraphWrapper).setKeyIndexableGraph(graph);
  }

  @Test
  public void addKeyIndexableGraphAddsANoOpKeyIndexableWrapperOfTheGraph() {
    // setup
    Graph graph = mock(Graph.class);

    // action
    instance.addKeyIndexableGraph(compositeGraphWrapper, graph);

    // verify
    verify(compositeGraphWrapper).setKeyIndexableGraph(any(NoOpKeyIndexableGraphWrapper.class));
  }
}
