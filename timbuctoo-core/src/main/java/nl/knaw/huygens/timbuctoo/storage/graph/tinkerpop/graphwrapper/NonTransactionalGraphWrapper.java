package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;

class NonTransactionalGraphWrapper implements GraphWrapper {

  public NonTransactionalGraphWrapper(Graph graph) {
    // TODO Auto-generated constructor stub
  }

  @Override
  public Features getFeatures() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public Vertex addVertex(Object id) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public Vertex getVertex(Object id) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void removeVertex(Vertex vertex) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public Iterable<Vertex> getVertices() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public Iterable<Vertex> getVertices(String key, Object value) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public Edge getEdge(Object id) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void removeEdge(Edge edge) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public Iterable<Edge> getEdges() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public Iterable<Edge> getEdges(String key, Object value) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public GraphQuery query() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void shutdown() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @SuppressWarnings("deprecation")
  @Override
  public void stopTransaction(Conclusion conclusion) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void commit() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void rollback() {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
