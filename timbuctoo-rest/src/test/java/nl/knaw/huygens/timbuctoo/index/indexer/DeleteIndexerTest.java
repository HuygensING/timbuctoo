package nl.knaw.huygens.timbuctoo.index.indexer;

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import org.mockito.InOrder;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class DeleteIndexerTest extends AbstractIndexerTest {
  @Override
  protected Indexer createInstance() {
    return new DeleteIndexer(repository, indexManager);
  }

  @Override
  protected void verifyIndexActionExecuted(Class<? extends DomainEntity> type, String id) throws IndexException {
    verify(indexManager).deleteEntity(type, id);
  }

  @Override
  protected void verifyIndexActionExecuted(InOrder inOrder, Class<? extends DomainEntity> type, String id) throws IndexException {
    inOrder.verify(indexManager).deleteEntity(type, id);
  }

  @Override
  protected void throwAnIndexExceptionWhenIndexMethodExecuted(Class<? extends DomainEntity> type, String id) throws IndexException {
    doThrow(IndexException.class).when(indexManager).deleteEntity(type, id);
  }
}
