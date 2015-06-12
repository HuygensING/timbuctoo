package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;

public class GraphWrapperFactory {

  public GraphWrapper wrap(Graph graph) {
    if (graph instanceof TransactionalGraph) {
      return new TransactionalGraphWrapper((TransactionalGraph) graph);
    }
    return new NonTransactionalGraphWrapper(graph);
  }

}
