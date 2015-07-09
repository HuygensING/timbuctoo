package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Graph;
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
    CompositeGraphWrapper graphWrapper = new CompositeGraphWrapper();

    transactionGraphWrapperFactory.addTransactionalGraph(graphWrapper, graph);
    keyIndexableGraphWrapperFactory.addKeyIndexableGraph(graphWrapper, graph);

    return graphWrapper;
  }

}
