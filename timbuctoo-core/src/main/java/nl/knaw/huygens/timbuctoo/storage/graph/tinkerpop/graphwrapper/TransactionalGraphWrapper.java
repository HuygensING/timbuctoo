package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;

class TransactionalGraphWrapper implements GraphWrapper {

  private TransactionalGraph delegate;

  public TransactionalGraphWrapper(TransactionalGraph delegate) {
    this.delegate = delegate;
  }

  @Override
  public Features getFeatures() {
    return delegate.getFeatures();
  }

  @Override
  public Vertex addVertex(Object id) {
    return delegate.addVertex(id);
  }

  @Override
  public Vertex getVertex(Object id) {
    return delegate.getVertex(id);
  }

  @Override
  public void removeVertex(Vertex vertex) {
    delegate.removeVertex(vertex);
  }

  @Override
  public Iterable<Vertex> getVertices() {
    return delegate.getVertices();
  }

  @Override
  public Iterable<Vertex> getVertices(String key, Object value) {
    return delegate.getVertices(key, value);
  }

  @Override
  public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
    return delegate.addEdge(id, outVertex, inVertex, label);
  }

  @Override
  public Edge getEdge(Object id) {
    return delegate.getEdge(id);
  }

  @Override
  public void removeEdge(Edge edge) {
    delegate.removeEdge(edge);
  }

  @Override
  public Iterable<Edge> getEdges() {
    return delegate.getEdges();
  }

  @Override
  public Iterable<Edge> getEdges(String key, Object value) {
    return delegate.getEdges(key, value);
  }

  @Override
  public GraphQuery query() {
    return delegate.query();
  }

  @Override
  public void shutdown() {
    delegate.shutdown();
  }

  static final String STOP_TRANSACTION_EXCEPTION_MESSAGE = "Use commit or rollback to close the transaction";

  @SuppressWarnings("deprecation")
  @Override
  public void stopTransaction(Conclusion conclusion) {
    throw new UnsupportedOperationException(STOP_TRANSACTION_EXCEPTION_MESSAGE);
  }

  @Override
  public void commit() {
    delegate.commit();
  }

  @Override
  public void rollback() {
    delegate.rollback();
  }

}
