package nl.knaw.huygens.timbuctoo.index.request;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CollectionIndexRequestTest extends AbstractIndexRequestTest {

  public static final String ID_1 = "id1";
  public static final String ID_2 = "id2";
  public static final ActionType ACTION_TYPE = ActionType.MOD;
  private RequestItemStatus requestItemStatus;

  @Override
  protected IndexRequest createInstance() {
    createRequestItemStatus();
    return new CollectionIndexRequest(ACTION_TYPE, TYPE, createRepository());
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

  @Test(expected = IndexException.class)
  public void executeThrowsAnIndexExceptionWhenTheIndexerDoes() throws Exception {
    // setup
    doThrow(IndexException.class).when(indexer).executeIndexAction(TYPE, ID_1);

    // action
    getInstance().execute(indexer);
  }

  @Override
  @Test
  public void toActionCreatesAnActionThatCanBeUsedByTheProducer() {
    // action
    Action action = getInstance().toAction();

    // verify
    assertThat(action, likeAction() //
      .withForMultiEntitiesFlag(true) //
      .withType(TYPE) //
      .withActionType(ACTION_TYPE));
  }

}
