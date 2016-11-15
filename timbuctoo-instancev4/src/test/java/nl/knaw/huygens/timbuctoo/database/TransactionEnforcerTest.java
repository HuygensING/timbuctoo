package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.handle.HandleAdder;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import org.junit.Test;
import org.mockito.InOrder;

import java.time.Clock;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class TransactionEnforcerTest {

  @Test
  public void executeAndReturnExecutesTheAfterSuccessTaskExecutorAfterTheTransactionIsCommitted() {
    AfterSuccessTaskExecutor afterSuccessTaskExecutor = mock(AfterSuccessTaskExecutor.class);
    DataStoreOperations dataStoreOperations = mock(DataStoreOperations.class);
    TransactionEnforcer instance =
      new TransactionEnforcer(() -> dataStoreOperations, new TimbuctooActions.TimbuctooActionsFactory(mock(
        Authorizer.class), Clock.systemDefaultZone(), mock(HandleAdder.class)), afterSuccessTaskExecutor);

    instance.executeAndReturn(timbuctooActions -> TransactionStateAndResult.commitAndReturn(""));

    InOrder inOrder = inOrder(dataStoreOperations, afterSuccessTaskExecutor);
    inOrder.verify(dataStoreOperations).success();
    inOrder.verify(dataStoreOperations).close();
    inOrder.verify(afterSuccessTaskExecutor).executeTasks();
  }

  @Test
  public void executeAndReturnDoesNotExecuteTheAfterSuccessTaskExecutorAfterTheTransactionIsRolledBack() {
    AfterSuccessTaskExecutor afterSuccessTaskExecutor = mock(AfterSuccessTaskExecutor.class);
    DataStoreOperations dataStoreOperations = mock(DataStoreOperations.class);
    TransactionEnforcer instance =
      new TransactionEnforcer(() -> dataStoreOperations, new TimbuctooActions.TimbuctooActionsFactory(mock(
        Authorizer.class), Clock.systemDefaultZone(), mock(HandleAdder.class)), afterSuccessTaskExecutor);

    instance.executeAndReturn(timbuctooActions -> TransactionStateAndResult.rollbackAndReturn(""));

    verifyZeroInteractions(afterSuccessTaskExecutor);
  }

  @Test(expected = RuntimeException.class)
  public void executeAndReturnDoesNotExecuteTheAfterSuccessTaskExecutorWhenAnExceptionIsThrown() {
    AfterSuccessTaskExecutor afterSuccessTaskExecutor = mock(AfterSuccessTaskExecutor.class);
    DataStoreOperations dataStoreOperations = mock(DataStoreOperations.class);
    TransactionEnforcer instance =
      new TransactionEnforcer(() -> dataStoreOperations, new TimbuctooActions.TimbuctooActionsFactory(mock(
        Authorizer.class), Clock.systemDefaultZone(), mock(HandleAdder.class)), afterSuccessTaskExecutor);

    try {
      instance.executeAndReturn(timbuctooActions -> {
        throw new RuntimeException();
      });
    } finally {
      verifyZeroInteractions(afterSuccessTaskExecutor);
    }


  }
}
