package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.*;

class KeyIndexableGraphWrapperFactory {
  public void addKeyIndexableGraph(CompositeGraphWrapper graphWrapper, Graph graph) {
    if (graph instanceof KeyIndexableGraph) {
      graphWrapper.setKeyIndexableGraph((KeyIndexableGraph) graph);
    } else {
      graphWrapper.setKeyIndexableGraph(new NoOpKeyIndexableGraphWrapper());
    }
  }

}
