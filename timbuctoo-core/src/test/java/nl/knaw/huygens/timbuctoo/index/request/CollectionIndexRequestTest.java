package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import org.junit.Test;
import org.mockito.InOrder;
import test.variation.model.projecta.ProjectADomainEntity;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CollectionIndexRequestTest extends AbstractIndexRequestTest {

  public static final String ID_1 = "id1";
  public static final String ID_2 = "id2";

  @Override
  protected IndexRequest createInstance() {
    return new CollectionIndexRequest(TYPE, createRepository(), requestedStatus);
  }

  private Repository createRepository() {
    Repository repository = mock(Repository.class);
    StorageIteratorStub<ProjectADomainEntity> interator = StorageIteratorStub.newInstance(createEntityWithId(ID_1), createEntityWithId(ID_2));
    when(repository.getDomainEntities(TYPE)).thenReturn(interator);

    return repository;
  }

  private ProjectADomainEntity createEntityWithId(String id) {
    return new ProjectADomainEntity(id);
  }

  @Test
  public void executeIndexesEveryEntityFoundByTheRepository() throws Exception {
    // action
    getInstance().execute(indexer);

    // verify
    verify(indexer).executeIndexAction(TYPE, ID_1);
    verify(indexer).executeIndexAction(TYPE, ID_2);
  }

  @Override
  protected void verifyIndexAction(InOrder inOrder) throws IndexException {
    inOrder.verify(indexer).executeIndexAction(TYPE, ID_1);
    inOrder.verify(indexer).executeIndexAction(TYPE, ID_2);
  }

  @Test(expected = IndexException.class)
  public void executeThrowsAnIndexExceptionWhenTheIndexerDoes() throws Exception {
    // setup
    doThrow(IndexException.class).when(indexer).executeIndexAction(TYPE, ID_1);

    // action
    getInstance().execute(indexer);
  }


}
