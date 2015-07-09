package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.*;

import java.util.Set;

class CompositeGraphWrapper extends AbstractGraphWrapper implements GraphWrapper {
  private final Graph graph;
  private final TransactionalGraph transactionalGraph;
  private final KeyIndexableGraph keyIndexableGraph;

  public CompositeGraphWrapper(Graph graph, TransactionalGraph transactionalGraph, KeyIndexableGraph keyIndexableGraph) {
    this.graph = graph;
    this.transactionalGraph = transactionalGraph;
    this.keyIndexableGraph = keyIndexableGraph;
  }

  @Override
  protected Graph getDelegate() {
    return this.graph;
  }

  @Override
  public void stopTransaction(Conclusion conclusion) {
    this.transactionalGraph.stopTransaction(conclusion);
  }

  @Override
  public void commit() {
    this.transactionalGraph.commit();
  }

  @Override
  public void rollback() {
    this.transactionalGraph.rollback();
  }

  @Override
  public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
    this.keyIndexableGraph.dropKeyIndex(key, elementClass);
  }

  @Override
  public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter... indexParameters) {
    this.keyIndexableGraph.createKeyIndex(key, elementClass);
  }

  @Override
  public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
    return this.keyIndexableGraph.getIndexedKeys(elementClass);
  }
}
