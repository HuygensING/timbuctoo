package nl.knaw.huygens.timbuctoo.persistence.request;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.persistence.Persister;
import nl.knaw.huygens.timbuctoo.persistence.persister.PersisterFactory;
import org.junit.Before;
import org.junit.Test;
import test.model.projecta.ProjectAPerson;

import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntityPersistenceRequestTest extends AbstractPersistenceRequestTest {

  public static final String ID = "id";
  public static final ProjectAPerson ENTITY = new ProjectAPerson();
  private EntityPersistenceRequest instance;
  private PersisterFactory persisterFactory;
  private Repository repository;

  @Before
  public void setUp() throws Exception {
    repository = mock(Repository.class);
    persisterFactory = mock(PersisterFactory.class);
    instance = new EntityPersistenceRequest(repository, persisterFactory, ACTION_TYPE, TYPE, ID);
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
      .withId(ID)));
  }

  @Test
  public void executeTheCreatedPersiterForTheEntityOfTheRequest() {
    // setup
    when(repository.getEntityOrDefaultVariation(TYPE, ID)).thenReturn(ENTITY);

    Persister persister = mock(Persister.class);
    when(persisterFactory.forActionType(ACTION_TYPE)).thenReturn(persister);

    // action
    instance.execute();

    // verify
    verify(persister).execute(ENTITY);
  }

}
