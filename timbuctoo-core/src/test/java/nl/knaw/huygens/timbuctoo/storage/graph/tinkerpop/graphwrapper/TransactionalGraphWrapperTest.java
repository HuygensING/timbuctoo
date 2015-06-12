package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.TransactionalGraph;

public class TransactionalGraphWrapperTest extends AbstractGraphWrapperTest {

  private TransactionalGraph delegate;
  private TransactionalGraphWrapper instance;

  @Before
  public void setup() {
    this.delegate = mock(TransactionalGraph.class);
    this.instance = new TransactionalGraphWrapper(getDelegate());
  }

  // transactional graph wrapper specific functionality

  @Test
  public void commitDelegatesTheCall() {
    // action
    getInstance().commit();

    // verify
    verify(getDelegate()).commit();
  }

  @Test
  public void rollbackDelegatesTheCall() {
    // action
    getInstance().rollback();

    // verify
    verify(getDelegate()).rollback();
  }

  // helper methods

  @Override
  protected TransactionalGraph getDelegate() {
    return delegate;
  }

  @Override
  protected TransactionalGraphWrapper getInstance() {
    return instance;
  }

}
