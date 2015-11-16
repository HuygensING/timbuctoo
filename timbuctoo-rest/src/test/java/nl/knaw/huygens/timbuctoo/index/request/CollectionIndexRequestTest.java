package nl.knaw.huygens.timbuctoo.index.request;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import org.junit.Test;
import org.mockito.InOrder;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CollectionIndexRequestTest extends AbstractIndexRequestTest {

  public static final String ID_1 = "id1";
  public static final String ID_2 = "id2";
  private RequestItemStatus requestItemStatus;

  @Override
  protected IndexRequest createInstance() {
    createRequestItemStatus();
    return new CollectionIndexRequest(TYPE, createRepository(), requestedStatus, requestItemStatus);
  }

  private void createRequestItemStatus() {
    requestItemStatus = mock(RequestItemStatus.class);
    when(requestItemStatus.getToDo()).thenReturn(Lists.newArrayList(ID_1, ID_2));
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

  @Test
  public void executeAddsAllTheFoundIdsToTheTodoListAndRemovesThemWhenDoneIndexing() throws Exception {
    // action
    getInstance().execute(indexer);

    // verify
    verify(requestItemStatus).setToDo(Lists.newArrayList(ID_1, ID_2));
    verify(requestItemStatus).done(ID_1);
    verify(requestItemStatus).done(ID_2);
  }


}
