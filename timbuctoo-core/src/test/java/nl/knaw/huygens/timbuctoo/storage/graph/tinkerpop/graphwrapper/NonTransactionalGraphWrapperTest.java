package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.graphwrapper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Graph;

public class NonTransactionalGraphWrapperTest extends AbstractGraphWrapperTest {

  private Graph delegate;
  private NonTransactionalGraphWrapper instance;

  @Before
  public void setup() {
    delegate = mock(Graph.class);
    instance = new NonTransactionalGraphWrapper(delegate);
  }

  @Test
  public void commitInitiatesNoInteractions() {
    // action
    instance.commit();

    // verify
    verifyNoMoreInteractions(getDelegate());
  }

  @Test
  public void rollbackInitiatesNoInteractions() {
    // action
    instance.rollback();

    // verify
    verifyNoMoreInteractions(getDelegate());
  }

  @Override
  protected NonTransactionalGraphWrapper getInstance() {
    return instance;
  }

  @Override
  protected Graph getDelegate() {
    return delegate;
  }

}
