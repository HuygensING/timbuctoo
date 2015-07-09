package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;


import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;

class TransactionalGraphWrapperFactory {
  public void addTransactionalGraph(CompositeGraphWrapper graphWrapper, Graph graph) {
    if(graph instanceof TransactionalGraph) {
      graphWrapper.setTranactionalGraph((TransactionalGraph) graph);
    }
    else{
      graphWrapper.setTranactionalGraph(new NoOpTransactionalGraphWrapper(graph));
    }
  }
}
