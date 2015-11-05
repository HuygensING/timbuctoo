package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;

public class GraphWrapperFactory {

  private TransactionalGraphFactory transactionGraphWrapperFactory;
  private KeyIndexableGraphFactory keyIndexableGraphFactory;

  public GraphWrapperFactory(){
    this(new TransactionalGraphFactory(), new KeyIndexableGraphFactory());
  }

  public GraphWrapperFactory(TransactionalGraphFactory transactionalGraphFactory, KeyIndexableGraphFactory keyIndexableGraphFactory) {
    this.transactionGraphWrapperFactory = transactionalGraphFactory;
    this.keyIndexableGraphFactory = keyIndexableGraphFactory;
  }

  public GraphWrapper wrap(Graph graph) {
    TransactionalGraph transactionalGraph = transactionGraphWrapperFactory.create(graph);
    KeyIndexableGraph keyIndexableGraph = keyIndexableGraphFactory.create(graph);

    CompositeGraphWrapper graphWrapper = new CompositeGraphWrapper(graph, transactionalGraph, keyIndexableGraph);

    return graphWrapper;
  }

}
