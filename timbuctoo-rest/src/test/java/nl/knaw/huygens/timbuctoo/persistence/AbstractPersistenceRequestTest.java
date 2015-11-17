package nl.knaw.huygens.timbuctoo.persistence;

import nl.knaw.huygens.timbuctoo.messages.ActionType;
import org.junit.Test;
import test.model.projecta.ProjectAPerson;

public abstract class AbstractPersistenceRequestTest {
  public static final ActionType ACTION_TYPE = ActionType.ADD;
  public static final Class<ProjectAPerson> TYPE = ProjectAPerson.class;

  @Test
  abstract void toActionCreatesAnActionThatCanBeUsedByTheProducer();
}
