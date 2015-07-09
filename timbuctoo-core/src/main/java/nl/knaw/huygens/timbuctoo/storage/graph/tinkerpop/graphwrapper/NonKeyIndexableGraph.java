package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.*;

import java.util.Set;

class NonKeyIndexableGraph extends AbstractGraphWrapper implements KeyIndexableGraph {
  private Graph graph;

  public NonKeyIndexableGraph(Graph graph) {
    this.graph = graph;
  }

  @Override
  public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
  }

  @Override
  public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter... indexParameters) {
  }

  @Override
  public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
    return Sets.newHashSet();
  }

  @Override
  protected Graph getDelegate() {
    return this.graph;
  }

}
