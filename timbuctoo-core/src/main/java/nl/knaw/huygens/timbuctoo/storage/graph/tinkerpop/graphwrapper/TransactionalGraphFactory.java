package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;


import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;

class TransactionalGraphFactory {
  public TransactionalGraph create(Graph graph) {
    if(graph instanceof TransactionalGraph) {
      return (TransactionalGraph) graph;
    }
    return new NonTransactionalGraph(graph);
  }
}
