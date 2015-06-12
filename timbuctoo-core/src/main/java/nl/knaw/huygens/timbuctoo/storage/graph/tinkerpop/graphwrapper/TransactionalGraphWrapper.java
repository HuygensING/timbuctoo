package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.TransactionalGraph;

public class TransactionalGraphWrapper extends AbstractGraphWrapper implements GraphWrapper {

  private TransactionalGraph delegate;

  public TransactionalGraphWrapper(TransactionalGraph delegate) {
    this.delegate = delegate;
  }

  @Override
  protected TransactionalGraph getDelegate() {
    return delegate;
  }

  @Override
  public void commit() {
    getDelegate().commit();
  }

  @Override
  public void rollback() {
    getDelegate().rollback();
  }

}
