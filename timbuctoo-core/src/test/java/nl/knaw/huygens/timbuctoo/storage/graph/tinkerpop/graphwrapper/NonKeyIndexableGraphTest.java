package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class NonKeyIndexableGraphTest extends AbstractGraphWrapperTest {

  public static final Class<Vertex> ELEMENT_CLASS = Vertex.class;
  public static final String KEY = "key";
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private NonKeyIndexableGraph instance;
  private Graph graph;

  @Before
  public void setup(){
    graph = mock(Graph.class);
    instance = new NonKeyIndexableGraph(graph);
  }


  @Test
  public void dropKeyIndexDoesNothing(){
    // action
    instance.dropKeyIndex(KEY, ELEMENT_CLASS);

    // verify
    verifyZeroInteractions(graph);
  }

  @Test
  public void createKeyIndexDoesNothing(){
    // action
    instance.createKeyIndex(KEY, ELEMENT_CLASS);

    // verify
    verifyZeroInteractions(graph);
  }

  @Test
  public void getIndexedKeysReturnsAnEmptySet(){
    // action
    Set<String> indexedKeys = instance.getIndexedKeys(ELEMENT_CLASS);

    // verify
    assertThat(indexedKeys, is(empty()));
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
