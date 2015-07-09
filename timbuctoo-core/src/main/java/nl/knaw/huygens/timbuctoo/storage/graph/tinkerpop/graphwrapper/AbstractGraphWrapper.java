package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;

public abstract class AbstractGraphWrapper implements Graph {

  protected abstract Graph getDelegate();

  @Override
  public Features getFeatures() {
    return getDelegate().getFeatures();
  }

  @Override
  public Vertex addVertex(Object id) {
    return getDelegate().addVertex(id);
  }

  @Override
  public Vertex getVertex(Object id) {
    return getDelegate().getVertex(id);
  }

  @Override
  public void removeVertex(Vertex vertex) {
    getDelegate().removeVertex(vertex);
  }

  @Override
  public Iterable<Vertex> getVertices() {
    return getDelegate().getVertices();
  }

  @Override
  public Iterable<Vertex> getVertices(String key, Object value) {
    return getDelegate().getVertices(key, value);
  }

  @Override
  public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
    return getDelegate().addEdge(id, outVertex, inVertex, label);
  }

  @Override
  public Edge getEdge(Object id) {
    return getDelegate().getEdge(id);
  }

  @Override
  public void removeEdge(Edge edge) {
    getDelegate().removeEdge(edge);
  }

  @Override
  public Iterable<Edge> getEdges() {
    return getDelegate().getEdges();
  }

  @Override
  public Iterable<Edge> getEdges(String key, Object value) {
    return getDelegate().getEdges(key, value);
  }

  @Override
  public GraphQuery query() {
    return getDelegate().query();
  }

  @Override
  public void shutdown() {
    getDelegate().shutdown();
  }

  public AbstractGraphWrapper() {
    super();
  }

}
