package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeMockBuilder.anEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.Vertex;

@SuppressWarnings("deprecation")
public class TransactionalGraphWrapperTest {

  private static final String ID = "id";
  private TransactionalGraph delegate;
  private TransactionalGraphWrapper instance;

  @Before
  public void setup() {
    delegate = mock(TransactionalGraph.class);
    instance = new TransactionalGraphWrapper(delegate);
  }

  @Test
  public void addEdgeDelegatesTheCall() {
    // setup
    Vertex outVertex = aVertex().build();
    Vertex inVertex = aVertex().build();
    String label = "label";

    Edge createdEdge = anEdge().build();
    when(delegate.addEdge(ID, outVertex, inVertex, "label")).thenReturn(createdEdge);

    // action
    Edge actualEdge = instance.addEdge(ID, outVertex, inVertex, label);

    // verify
    assertIsSameInstance(createdEdge, actualEdge);
    verify(delegate).addEdge(ID, outVertex, inVertex, "label");
  }

  @Test
  public void addVertexDelegatesTheCall() {
    // setup
    Vertex createdVertex = aVertex().build();
    when(delegate.addVertex(null)).thenReturn(createdVertex);

    // action
    Vertex actualVertex = instance.addVertex(null);

    // verify
    assertIsSameInstance(createdVertex, actualVertex);
    verify(delegate).addVertex(null);
  }

  @Test
  public void getEdgeDelegatesTheCall() {
    // setup
    Edge foundEdge = anEdge().build();
    when(delegate.getEdge(ID)).thenReturn(foundEdge);

    // action
    Edge actualEdge = instance.getEdge(ID);

    // verify
    assertIsSameInstance(actualEdge, foundEdge);
    verify(delegate).getEdge(ID);
  }

  @Test
  public void getEdgesDelegatesTheCall() {
    // setup
    Iterable<Edge> edges = Lists.newArrayList();
    when(delegate.getEdges()).thenReturn(edges);

    // action
    Iterable<Edge> actualEdges = instance.getEdges();

    // verify
    assertIsSameInstance(edges, actualEdges);
    verify(delegate).getEdges();
  }

  @Test
  public void getEdgesForKeyWithValueDelegatesTheCall() {
    // setup
    String key = "key";
    String value = "value";
    Iterable<Edge> edges = Lists.newArrayList();
    when(delegate.getEdges(key, value)).thenReturn(edges);

    // action
    Iterable<Edge> actualEdges = instance.getEdges(key, value);

    // verify
    assertIsSameInstance(edges, actualEdges);
    verify(delegate).getEdges(key, value);
  }

  @Test
  public void getFeaturesDelegatesTheCall() {
    // setup
    Features expected = mock(Features.class);
    when(delegate.getFeatures()).thenReturn(expected);

    // action
    Features actual = instance.getFeatures();

    // verify
    assertIsSameInstance(expected, actual);
    verify(delegate).getFeatures();
  }

  @Test
  public void getVertexDelegatesTheCall() {
    // setup
    Vertex expected = aVertex().build();
    when(instance.getVertex(ID)).thenReturn(expected);

    // action
    Vertex actual = instance.getVertex(ID);

    // verify
    assertIsSameInstance(expected, actual);
    verify(delegate).getVertex(ID);
  }

  @Test
  public void getVerticesDelegatesTheCall() {
    // setup
    Iterable<Vertex> expected = Lists.newArrayList();
    when(delegate.getVertices()).thenReturn(expected);

    // action
    Iterable<Vertex> actual = instance.getVertices();

    // verify
    assertIsSameInstance(expected, actual);
    verify(delegate).getVertices();
  }

  @Test
  public void getVerticesForKeyWithValueDelegatesTheCall() {
    // setup
    String key = "key";
    String value = "value";
    Iterable<Vertex> expected = Lists.newArrayList();
    when(delegate.getVertices(key, value)).thenReturn(expected);

    // action
    Iterable<Vertex> actual = instance.getVertices(key, value);

    // verify
    assertIsSameInstance(expected, actual);
    verify(delegate).getVertices(key, value);
  }

  @Test
  public void queryDelegatesTheCall() {
    // setup
    GraphQuery expected = mock(GraphQuery.class);
    when(delegate.query()).thenReturn(expected);

    // action
    GraphQuery actual = instance.query();

    // verify
    assertIsSameInstance(expected, actual);
    verify(delegate).query();
  }

  @Test
  public void removeEdgeDelegatesTheCall() {
    // setup
    Edge edge = anEdge().build();

    // action
    instance.removeEdge(edge);

    // verify
    verify(delegate).removeEdge(edge);
  }

  @Test
  public void removeVertexDelegatesTheCall() {
    // setup
    Vertex vertex = aVertex().build();

    // action
    instance.removeVertex(vertex);

    // verify
    verify(delegate).removeVertex(vertex);
  }

  @Test
  public void shutdownDelegatesTheCall() {
    // action
    instance.shutdown();

    // verify
    verify(delegate).shutdown();
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void stopTransactionThrowsAnUnsupportedOperationException() {
    // setup
    expectedException.expect(UnsupportedOperationException.class);
    expectedException.expectMessage(TransactionalGraphWrapper.STOP_TRANSACTION_EXCEPTION_MESSAGE);

    // action
    instance.stopTransaction(Conclusion.SUCCESS);

  }

  // transactional graph wrapper specific functionality

  @Test
  public void commitDelegatesTheCall() {
    // action
    instance.commit();

    // verify
    verify(delegate).commit();
  }

  @Test
  public void rollbackDelegatesTheCall() {
    // action
    instance.rollback();

    // verify
    verify(delegate).rollback();
  }

  // helper methods

  private <T> void assertIsSameInstance(T expected, T actual) {
    assertThat(actual, is(sameInstance(expected)));
  }

}
