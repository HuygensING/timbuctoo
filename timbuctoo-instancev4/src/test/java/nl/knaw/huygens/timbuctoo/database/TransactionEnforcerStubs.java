package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;

public class TransactionEnforcerStubs {

  public static TransactionEnforcer forGraphWrapper(TinkerPopGraphManager graphManager) {
    return new TransactionEnforcer(
      () -> DataStoreOperationsStubs.forGraphWrapper(graphManager),
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
