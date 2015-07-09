package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Graph;

public class GraphWrapperFactoryTest {
  private GraphWrapperFactory instance;
  private TransactionalGraphWrapperFactory transactionalGraphWrapperFactory;
  private KeyIndexableGraphFactory keyIndexableGraphFactory;

  @Before
  public void setup() {
    transactionalGraphWrapperFactory = mock(TransactionalGraphWrapperFactory.class);
    keyIndexableGraphFactory = mock(KeyIndexableGraphFactory.class);
    instance = new GraphWrapperFactory(transactionalGraphWrapperFactory, keyIndexableGraphFactory);
  }

  @Test
  public void wrapCreatesACompositeGraphWrapperAndLetsTheOtherGraphWrappersAddTheSpecificGraphWrappers() {
    // setup
    Graph graph = mock(Graph.class);

    // action
    GraphWrapper graphWrapper = instance.wrap(graph);

    // verify
    assertThat(graphWrapper, is(instanceOf(CompositeGraphWrapper.class)));

    verify(transactionalGraphWrapperFactory).wrap(graph);
    verify(keyIndexableGraphFactory).create(graph);
  }

}
