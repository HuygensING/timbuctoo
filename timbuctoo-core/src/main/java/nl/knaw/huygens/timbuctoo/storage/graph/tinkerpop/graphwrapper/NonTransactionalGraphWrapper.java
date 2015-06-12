package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import com.tinkerpop.blueprints.Graph;

class NonTransactionalGraphWrapper extends AbstractGraphWrapper {

  private Graph delegate;

  public NonTransactionalGraphWrapper(Graph delegate) {
    this.delegate = delegate;
  }

  @Override
  public void commit() {
    // Yet to be implemented, see TIM-239.
  }

  @Override
  public void rollback() {
    // Yet to be implemented, see TIM-239.   
  }

  @Override
  protected Graph getDelegate() {
    return delegate;
  }

}
