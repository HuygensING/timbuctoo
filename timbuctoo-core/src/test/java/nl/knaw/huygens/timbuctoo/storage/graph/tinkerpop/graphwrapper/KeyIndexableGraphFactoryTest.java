package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class KeyIndexableGraphFactoryTest {

  private CompositeGraphWrapper compositeGraphWrapper;
  private KeyIndexableGraphFactory instance;

  @Before
  public void setup() {
    compositeGraphWrapper = mock(CompositeGraphWrapper.class);

    instance = new KeyIndexableGraphFactory();
  }

  @Test
  public void wrapReturnsTheGraphWhenItIsAKeyIndexableGraph() {
    // setup
    KeyIndexableGraph graph = mock(KeyIndexableGraph.class);

    // action
    KeyIndexableGraph returnedGraph = instance.create(graph);

    // verify
    assertThat(returnedGraph, is(sameInstance(graph)));
  }

  @Test
  public void wrapReturnsANoOpKeyIndexableGraphWrapperWhenItIsANotKeyIndexableGraph() {
    // setup
    Graph graph = mock(Graph.class);

    // action
    KeyIndexableGraph returnedGraph = instance.create(graph);

    // verify
    assertThat(returnedGraph, is(instanceOf(NonKeyIndexableGraph.class)));
  }

}
