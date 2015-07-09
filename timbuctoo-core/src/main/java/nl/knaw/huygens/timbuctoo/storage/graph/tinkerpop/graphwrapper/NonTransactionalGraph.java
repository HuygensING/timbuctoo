package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.*;

public class NonTransactionalGraph extends AbstractGraphWrapper implements TransactionalGraph {
  public static final String STOP_TRANSACTION_EXCEPTION_MESSAGE = "Use commit or rollback to close the transaction";

  private Graph graph;

  public NonTransactionalGraph(Graph graph) {
    this.graph = graph;
  }

  @Override
  public void stopTransaction(Conclusion conclusion) {
    throw new UnsupportedOperationException(STOP_TRANSACTION_EXCEPTION_MESSAGE);
  }

  @Override
  public void commit() {
  }

  @Override
  public void rollback() {
  }

  @Override
  protected Graph getDelegate() {
    return this.graph;
  }
}
