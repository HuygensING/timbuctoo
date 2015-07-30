package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.*;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeMockBuilder.anEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.*;

@SuppressWarnings("deprecation")
public abstract class AbstractGraphWrapperTest {

  private static final String ID = "id";

  protected abstract AbstractGraphWrapper getInstance();

  protected abstract Graph getDelegate();

  @Test
  public void addEdgeDelegatesTheCall() {
    // setup
    Vertex outVertex = aVertex().build();
    Vertex inVertex = aVertex().build();
    String label = "label";

    Edge createdEdge = anEdge().build();
    when(getDelegate().addEdge(ID, outVertex, inVertex, "label")).thenReturn(createdEdge);

    // action
    Edge actualEdge = getInstance().addEdge(ID, outVertex, inVertex, label);

    // verify
    assertIsSameInstance(createdEdge, actualEdge);
    verify(getDelegate()).addEdge(ID, outVertex, inVertex, "label");
  }

  @Test
  public void addVertexDelegatesTheCall() {
    // setup
    Vertex createdVertex = aVertex().build();
    when(getDelegate().addVertex(null)).thenReturn(createdVertex);

    // action
    Vertex actualVertex = getInstance().addVertex(null);

    // verify
    assertIsSameInstance(createdVertex, actualVertex);
    verify(getDelegate()).addVertex(null);
  }

  @Test
  public void getEdgeDelegatesTheCall() {
    // setup
    Edge foundEdge = anEdge().build();
    when(getDelegate().getEdge(ID)).thenReturn(foundEdge);

    // action
    Edge actualEdge = getInstance().getEdge(ID);

    // verify
    assertIsSameInstance(actualEdge, foundEdge);
    verify(getDelegate()).getEdge(ID);
  }

  @Test
  public void getEdgesDelegatesTheCall() {
    // setup
    Iterable<Edge> edges = Lists.newArrayList();
    when(getDelegate().getEdges()).thenReturn(edges);

    // action
    Iterable<Edge> actualEdges = getInstance().getEdges();

    // verify
    assertIsSameInstance(edges, actualEdges);
    verify(getDelegate()).getEdges();
  }

  @Test
  public void getEdgesForKeyWithValueDelegatesTheCall() {
    // setup
    String key = "KEY";
    String value = "value";
    Iterable<Edge> edges = Lists.newArrayList();
    when(getDelegate().getEdges(key, value)).thenReturn(edges);

    // action
    Iterable<Edge> actualEdges = getInstance().getEdges(key, value);

    // verify
    assertIsSameInstance(edges, actualEdges);
    verify(getDelegate()).getEdges(key, value);
  }

  @Test
  public void getFeaturesDelegatesTheCall() {
    // setup
    Features expected = mock(Features.class);
    when(getDelegate().getFeatures()).thenReturn(expected);

    // action
    Features actual = getInstance().getFeatures();

    // verify
    assertIsSameInstance(expected, actual);
    verify(getDelegate()).getFeatures();
  }

  @Test
  public void getVertexDelegatesTheCall() {
    // setup
    Vertex expected = aVertex().build();
    when(getInstance().getVertex(ID)).thenReturn(expected);

    // action
    Vertex actual = getInstance().getVertex(ID);

    // verify
    assertIsSameInstance(expected, actual);
    verify(getDelegate()).getVertex(ID);
  }

  @Test
  public void getVerticesDelegatesTheCall() {
    // setup
    Iterable<Vertex> expected = Lists.newArrayList();
    when(getDelegate().getVertices()).thenReturn(expected);

    // action
    Iterable<Vertex> actual = getInstance().getVertices();

    // verify
    assertIsSameInstance(expected, actual);
    verify(getDelegate()).getVertices();
  }

  @Test
  public void getVerticesForKeyWithValueDelegatesTheCall() {
    // setup
    String key = "KEY";
    String value = "value";
    Iterable<Vertex> expected = Lists.newArrayList();
    when(getDelegate().getVertices(key, value)).thenReturn(expected);

    // action
    Iterable<Vertex> actual = getInstance().getVertices(key, value);

    // verify
    assertIsSameInstance(expected, actual);
    verify(getDelegate()).getVertices(key, value);
  }

  @Test
  public void queryDelegatesTheCall() {
    // setup
    GraphQuery expected = mock(GraphQuery.class);
    when(getDelegate().query()).thenReturn(expected);

    // action
    GraphQuery actual = getInstance().query();

    // verify
    assertIsSameInstance(expected, actual);
    verify(getDelegate()).query();
  }

  @Test
  public void removeEdgeDelegatesTheCall() {
    // setup
    Edge edge = anEdge().build();

    // action
    getInstance().removeEdge(edge);

    // verify
    verify(getDelegate()).removeEdge(edge);
  }

  @Test
  public void removeVertexDelegatesTheCall() {
    // setup
    Vertex vertex = aVertex().build();

    // action
    getInstance().removeVertex(vertex);

    // verify
    verify(getDelegate()).removeVertex(vertex);
  }

  @Test
  public void shutdownDelegatesTheCall() {
    // action
    getInstance().shutdown();

    // verify
    verify(getDelegate()).shutdown();
  }

  public AbstractGraphWrapperTest() {
    super();
  }

  private <T> void assertIsSameInstance(T expected, T actual) {
    assertThat(actual, is(sameInstance(expected)));
  }

}
