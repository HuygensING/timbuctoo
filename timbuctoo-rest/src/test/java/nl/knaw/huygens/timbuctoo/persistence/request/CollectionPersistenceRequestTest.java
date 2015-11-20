package nl.knaw.huygens.timbuctoo.persistence.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import nl.knaw.huygens.timbuctoo.persistence.PersisterFactory;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import org.junit.Before;
import org.junit.Test;
import test.model.projecta.ProjectAPerson;

import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CollectionPersistenceRequestTest extends AbstractPersistenceRequestTest{

  public static final ProjectAPerson ENTITY_1 = new ProjectAPerson();
  public static final ProjectAPerson ENTITY_2 = new ProjectAPerson();
  private CollectionPersistenceRequest instance;
  private PersisterFactory persisterFactory;
  private Repository repository;

  @Before
  public void setUp() throws Exception {
    repository = mock(Repository.class);
    persisterFactory = mock(PersisterFactory.class);
    instance = new CollectionPersistenceRequest(repository, persisterFactory, ACTION_TYPE, TYPE);
  }

  @Override
  @Test
  public void toActionCreatesAnActionThatCanBeUsedByTheProducer() {
    // action
    Action action = instance.toAction();

    // verify
    assertThat(action, is(likeAction() //
      .withActionType(ACTION_TYPE) //
      .withType(TYPE) //
      .withForMultiEntitiesFlag(true)));
  }

  @Test
  public void executeTheCreatedPersiterForEachItemInTheCollectionToPersist(){
    // setup
    StorageIterator<ProjectAPerson> collection = StorageIteratorStub.newInstance(ENTITY_1, ENTITY_2);
    when(repository.getDomainEntities(TYPE)).thenReturn(collection);

    Persister persister = mock(Persister.class);
    when(persisterFactory.forActionType(ACTION_TYPE)).thenReturn(persister);

    // action
    instance.execute();

    // verify
    verify(persister).execute(ENTITY_1);
    verify(persister).execute(ENTITY_2);
  }

}
