package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.*;

class CompositeGraphWrapper implements GraphWrapper {
  private final TransactionalGraph transactionalGraph;
  private KeyIndexableGraph keyIndexableGraph;
  private TransactionalGraph tranactionalGraph;

  public CompositeGraphWrapper(TransactionalGraph transactionalGraph, KeyIndexableGraph keyIndexableGraph) {
    this.transactionalGraph = transactionalGraph;
    this.keyIndexableGraph = keyIndexableGraph;
  }

  @Override
  public void stopTransaction(Conclusion conclusion) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void commit() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void rollback() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Features getFeatures() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Vertex addVertex(Object id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Vertex getVertex(Object id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void removeVertex(Vertex vertex) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Iterable<Vertex> getVertices() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Iterable<Vertex> getVertices(String key, Object value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Edge getEdge(Object id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void removeEdge(Edge edge) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Iterable<Edge> getEdges() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Iterable<Edge> getEdges(String key, Object value) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public GraphQuery query() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void shutdown() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
