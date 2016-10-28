package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.database.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;

import java.util.function.Function;
import java.util.function.Supplier;

public class TransactionEnforcer {

  private final Supplier<DataStoreOperations> dataStoreOperationsSupplier;
  private final TimbuctooActions.TimbuctooActionsFactory timbuctooActionsFactory;
  private final AfterSuccessTaskExecutor afterSuccessTaskExecutor;

  public TransactionEnforcer(Supplier<DataStoreOperations> dataStoreOperationsSupplier,
                             TimbuctooActions.TimbuctooActionsFactory timbuctooActionsFactory) {
    this(dataStoreOperationsSupplier, timbuctooActionsFactory, new AfterSuccessTaskExecutor());
  }

  public TransactionEnforcer(Supplier<DataStoreOperations> dataStoreOperationsSupplier,
                             TimbuctooActions.TimbuctooActionsFactory timbuctooActionsFactory,
                             AfterSuccessTaskExecutor afterSuccessTaskExecutor) {

    this.dataStoreOperationsSupplier = dataStoreOperationsSupplier;
    this.timbuctooActionsFactory = timbuctooActionsFactory;
    this.afterSuccessTaskExecutor = afterSuccessTaskExecutor;
  }

  /**
   * @deprecated use {@link #executeAndReturn(Function)}
   */
  @Deprecated
  public <T> T oldExecuteAndReturn(Function<DataStoreOperations, TransactionStateAndResult<T>> actions) {
    DataStoreOperations db = dataStoreOperationsSupplier.get();

    try {
      TransactionStateAndResult<T> result = actions.apply(db);
      if (result.wasCommitted()) {
        db.success();
      } else {
        db.rollback();
      }
      return result.getValue();
    } catch (RuntimeException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

  public <T> T executeAndReturn(Function<TimbuctooActions, TransactionStateAndResult<T>> action) {
    DataStoreOperations db = dataStoreOperationsSupplier.get();

    boolean success = false;
    TimbuctooActions timbuctooActions = timbuctooActionsFactory.create(db, afterSuccessTaskExecutor);
    try {
      TransactionStateAndResult<T> result = action.apply(timbuctooActions);
      if (result.wasCommitted()) {
        success = true;
        db.success();
      } else {
        success = false;
        db.rollback();
      }
      return result.getValue();
    } catch (RuntimeException e) {
      success = false;
      db.rollback();
      throw e;
    } finally {
      db.close();
      if (success) {
        afterSuccessTaskExecutor.executeTasks();
      }
    }
  }

  public void execute(Function<DataStoreOperations, TransactionState> actions) {
    DataStoreOperations db = dataStoreOperationsSupplier.get();

    try {
      TransactionState result = actions.apply(db);
      if (result.wasCommitted()) {
        db.success();
      } else {
        db.rollback();
      }
    } catch (RuntimeException e) {
      db.rollback();
      throw e;
    } finally {
      db.close();
    }
  }

}
