package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.index.IndexException;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class EntityIndexRequestTest extends AbstractIndexRequestTest {

  private static final String ID = "id";

  @Override
  protected IndexRequest createInstance() {
    return new EntityIndexRequest(TYPE, ID, requestedStatus);
  }

  @Override
  protected void verifyIndexAction(InOrder inOrder) throws IndexException {
    inOrder.verify(indexer).executeIndexAction(TYPE, ID);
  }

  @Test
  public void executeLetsTheIndexerExecuteAnIndexAction() throws Exception {
    // action
    getInstance().execute(indexer);

    // verify
    verify(indexer).executeIndexAction(TYPE, ID);
  }

  @Test(expected = IndexException.class)
  public void executeThrowsAnIndexExceptionWhenTheIndexerDoes() throws Exception {
    // setup
    doThrow(IndexException.class).when(indexer).executeIndexAction(TYPE, ID);

    // action
    getInstance().execute(indexer);
  }


}
