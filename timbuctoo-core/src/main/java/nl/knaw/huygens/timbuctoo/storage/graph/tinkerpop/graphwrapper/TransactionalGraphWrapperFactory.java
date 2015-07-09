package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;


import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;

class TransactionalGraphWrapperFactory {
  public TransactionalGraph wrap(Graph graph) {
    if(graph instanceof TransactionalGraph) {
      return (TransactionalGraph) graph;
    }
    return new NoOpTransactionalGraphWrapper(graph);
  }
}
