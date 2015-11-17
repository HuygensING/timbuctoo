package nl.knaw.huygens.timbuctoo.persistence;

import nl.knaw.huygens.timbuctoo.messages.Action;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CollectionPersistenceRequestTest extends AbstractPersistenceRequestTest{

  @Override
  @Test
  public void toActionCreatesAnActionThatCanBeUsedByTheProducer() {
    // setup
    CollectionPersistenceRequest instance = new CollectionPersistenceRequest(ACTION_TYPE, TYPE);

    // action
    Action action = instance.toAction();

    // verify
    assertThat(action, is(likeAction() //
      .withActionType(ACTION_TYPE) //
      .withType(TYPE) //
      .withForMultiEntitiesFlag(true)));

  }

}
