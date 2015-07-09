package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;

public class GraphWrapperFactory {

  private TransactionalGraphWrapperFactory transactionGraphWrapperFactory;
  private KeyIndexableGraphFactory keyIndexableGraphFactory;

  public GraphWrapperFactory(){
    this(new TransactionalGraphWrapperFactory(), new KeyIndexableGraphFactory());
  }

  public GraphWrapperFactory(TransactionalGraphWrapperFactory transactionalGraphWrapperFactory, KeyIndexableGraphFactory keyIndexableGraphFactory) {
    this.transactionGraphWrapperFactory = transactionalGraphWrapperFactory;
    this.keyIndexableGraphFactory = keyIndexableGraphFactory;
  }

  public GraphWrapper wrap(Graph graph) {
    TransactionalGraph transactionalGraph = transactionGraphWrapperFactory.wrap(graph);
    KeyIndexableGraph keyIndexableGraph = keyIndexableGraphFactory.create(graph);

    CompositeGraphWrapper graphWrapper = new CompositeGraphWrapper(graph, transactionalGraph, keyIndexableGraph);

    return graphWrapper;
  }

}
