package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.net.URI;
import java.time.Clock;

import static org.mockito.Mockito.mock;

public class TransactionEnforcerStubs {

  public static TransactionEnforcer forGraphWrapper(GraphWrapper graphWrapper) {
    TimbuctooActions.TimbuctooActionsFactory timbuctooActionsFactory =
      new TimbuctooActions.TimbuctooActionsFactoryImpl(mock(Authorizer.class), Clock.systemDefaultZone(),
        mock(PersistentUrlCreator.class), (coll, id, rev) -> URI.create("http://example.org/persistent")
      );
    return new TransactionEnforcer(
      () -> new DataStoreOperations(graphWrapper, mock(ChangeListener.class), null, null),
      timbuctooActionsFactory
    );
  }

  public static TransactionEnforcer forDataStoreOperations(DataStoreOperations db) {
    TimbuctooActions.TimbuctooActionsFactory timbuctooActionsFactory =
      new TimbuctooActions.TimbuctooActionsFactoryImpl(mock(Authorizer.class), Clock.systemDefaultZone(),
        mock(PersistentUrlCreator.class), (coll, id, rev) -> URI.create("http://example.org/persistent")
      );
    TransactionEnforcer transactionEnforcer = new TransactionEnforcer(() -> db, timbuctooActionsFactory);
    return transactionEnforcer;
  }
}
