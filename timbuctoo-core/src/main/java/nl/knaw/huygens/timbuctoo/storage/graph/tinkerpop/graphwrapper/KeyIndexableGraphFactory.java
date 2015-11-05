package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.*;

class KeyIndexableGraphFactory {
  public KeyIndexableGraph create(Graph graph) {
    if(graph instanceof KeyIndexableGraph) {
      return (KeyIndexableGraph) graph;
    }

    return new NonKeyIndexableGraph(graph);
  }
}
