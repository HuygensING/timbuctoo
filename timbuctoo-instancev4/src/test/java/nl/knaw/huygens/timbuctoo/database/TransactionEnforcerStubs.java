package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import static org.mockito.Mockito.mock;

public class TransactionEnforcerStubs {

  public static TransactionEnforcer forGraphWrapper(GraphWrapper graphWrapper) {
    return new TransactionEnforcer(
      () -> new DataStoreOperations(graphWrapper, mock(ChangeListener.class), null, null),
      TimbuctooActionsStubs::withDataStoreAndAfterSucces
    );
  }

  public static TransactionEnforcer forDataStoreOperations(DataStoreOperations db) {
    return new TransactionEnforcer(
      () -> db,
      TimbuctooActionsStubs::withDataStoreAndAfterSucces
    );
  }
}
