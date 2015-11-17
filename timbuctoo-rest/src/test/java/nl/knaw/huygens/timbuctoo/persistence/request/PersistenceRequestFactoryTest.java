package nl.knaw.huygens.timbuctoo.persistence.request;

import nl.knaw.huygens.timbuctoo.messages.ActionType;
import nl.knaw.huygens.timbuctoo.persistence.PersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.request.CollectionPersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.request.EntityPersistenceRequest;
import nl.knaw.huygens.timbuctoo.persistence.request.PersistenceRequestFactory;
import org.junit.Test;
import test.model.projecta.ProjectAPerson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class PersistenceRequestFactoryTest {

  public static final ActionType ACTION_TYPE = ActionType.ADD;
  public static final Class<ProjectAPerson> TYPE = ProjectAPerson.class;
  public static final String ID = "id";

  @Test
  public void forEntityCreatesAnEntityPersistenceRequest() throws Exception {
    // setup
    PersistenceRequestFactory instance = new PersistenceRequestFactory();

    // action
    PersistenceRequest persistenceRequest = instance.forEntity(ACTION_TYPE, TYPE, ID);

    // verify
    assertThat(persistenceRequest, is(instanceOf(EntityPersistenceRequest.class)));
  }

  @Test
  public void forCollectionCreatesACollectionPersistenceRequest(){
    // setup
    PersistenceRequestFactory instance = new PersistenceRequestFactory();

    // action
    PersistenceRequest persistenceRequest = instance.forCollection(ACTION_TYPE, TYPE);

    // verify
    assertThat(persistenceRequest, is(instanceOf(CollectionPersistenceRequest.class)));
  }
}
