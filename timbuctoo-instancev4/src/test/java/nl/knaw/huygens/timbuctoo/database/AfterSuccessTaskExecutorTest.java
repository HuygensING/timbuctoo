package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.handle.HandleAdder;
import nl.knaw.huygens.timbuctoo.handle.HandleAdderParameters;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AfterSuccessTaskExecutorTest {
  @Test
  public void addHandleTaskAddsATaskToAddAHandle() {
    AfterSuccessTaskExecutor instance = new AfterSuccessTaskExecutor();
    HandleAdder handleAdder = mock(HandleAdder.class);
    HandleAdderParameters handleAdderParameters = new HandleAdderParameters(null, null, 0);

    instance.addHandleTask(handleAdder, handleAdderParameters);
    instance.executeTasks();

    verify(handleAdder).add(handleAdderParameters);
  }
}
