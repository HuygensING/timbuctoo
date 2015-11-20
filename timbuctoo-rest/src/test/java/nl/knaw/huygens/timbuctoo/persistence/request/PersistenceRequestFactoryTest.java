package nl.knaw.huygens.timbuctoo.persistence.request;

import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import org.junit.Before;
import org.junit.Test;
import test.model.projecta.ProjectAPerson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class PersistenceRequestFactoryTest {

  public static final ActionType ACTION_TYPE = ActionType.ADD;
  public static final Class<ProjectAPerson> TYPE = ProjectAPerson.class;
  public static final String ID = "id";
  private PersistenceRequestFactory instance;

  @Before
  public void setUp() throws Exception {
    instance = new PersistenceRequestFactory();
  }

  @Test
  public void forEntityCreatesAnEntityPersistenceRequest() throws Exception {
    // action
    PersistenceRequest persistenceRequest = instance.forEntity(ACTION_TYPE, TYPE, ID);

    // verify
    assertThat(persistenceRequest, is(instanceOf(EntityPersistenceRequest.class)));
  }

  @Test
  public void forCollectionCreatesACollectionPersistenceRequest(){
    // action
    PersistenceRequest persistenceRequest = instance.forCollection(ACTION_TYPE, TYPE);

    // verify
    assertThat(persistenceRequest, is(instanceOf(CollectionPersistenceRequest.class)));
  }

  @Test
  public void forActionCreatesAnEntityPersistenceRequestIfTheActionIsForASingleEntity(){
    // setup
    Action actionForSingleEntity = new Action(ACTION_TYPE, TYPE, ID);

    // action
    PersistenceRequest persistenceRequest = instance.forAction(actionForSingleEntity);

    // verify
    assertThat(persistenceRequest, is(instanceOf(EntityPersistenceRequest.class)));
  }

  @Test
  public void forActionCreatesACollectionPersistenceRequestIfTheActionIsForMultipleEntities(){
    // setup
    Action actionForMultipleEntities = new Action(ACTION_TYPE, TYPE);

    // action
    PersistenceRequest persistenceRequest = instance.forAction(actionForMultipleEntities);

    // verify
    assertThat(persistenceRequest, is(instanceOf(CollectionPersistenceRequest.class)));
  }
}
