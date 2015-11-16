package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.messages.Action;
import nl.knaw.huygens.timbuctoo.messages.ActionType;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.rest.resources.ActionMatcher.likeAction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class EntityIndexRequestTest extends AbstractIndexRequestTest {

  private static final String ID = "id";
  public static final ActionType ACTION_TYPE = ActionType.MOD;

  @Override
  protected IndexRequest createInstance() {
    return new EntityIndexRequest(ACTION_TYPE, TYPE, ID);
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

  @Override
  @Test
  public void toActionCreatesAnActionThatCanBeUsedByTheProducer() {
    // action
    Action action = getInstance().toAction();

    // verify
    assertThat(action, likeAction()//
      .withActionType(ACTION_TYPE) //
      .withType(TYPE) //
      .withId(ID));
  }
}
