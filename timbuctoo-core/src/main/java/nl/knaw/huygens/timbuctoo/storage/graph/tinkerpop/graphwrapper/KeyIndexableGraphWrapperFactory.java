package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.*;

class KeyIndexableGraphWrapperFactory {
  public KeyIndexableGraph wrap(Graph graph) {
    if(graph instanceof KeyIndexableGraph) {
      return (KeyIndexableGraph) graph;
    }

    return new NoOpKeyIndexableGraphWrapper(graph);
  }
}
