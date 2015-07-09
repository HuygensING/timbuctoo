package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;

public class GraphWrapperFactory {

  private TransactionalGraphWrapperFactory transactionGraphWrapperFactory;
  private KeyIndexableGraphWrapperFactory keyIndexableGraphWrapperFactory;

  public GraphWrapperFactory(){
    this(new TransactionalGraphWrapperFactory(), new KeyIndexableGraphWrapperFactory());
  }

  public GraphWrapperFactory(TransactionalGraphWrapperFactory transactionalGraphWrapperFactory, KeyIndexableGraphWrapperFactory keyIndexableGraphWrapperFactory) {
    this.transactionGraphWrapperFactory = transactionalGraphWrapperFactory;
    this.keyIndexableGraphWrapperFactory = keyIndexableGraphWrapperFactory;
  }

  public GraphWrapper wrap(Graph graph) {
    TransactionalGraph transactionalGraph = transactionGraphWrapperFactory.wrap(graph);
    KeyIndexableGraph keyIndexableGraph = keyIndexableGraphWrapperFactory.wrap(graph);

    CompositeGraphWrapper graphWrapper = new CompositeGraphWrapper(transactionalGraph, keyIndexableGraph);

    return graphWrapper;
  }

}
