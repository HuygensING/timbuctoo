package nl.knaw.huygens.timbuctoo.core;

import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopOperations;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.TinkerPopOperationsStubs;
import nl.knaw.huygens.timbuctoo.experimental.womenwriters.WomenWritersJsonCrudServiceTest;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;

import java.net.URI;
import java.time.Clock;

import static org.mockito.Mockito.mock;

public class TimbuctooActionsStubs {

  public static TimbuctooActions withDataStore(TinkerPopOperations tinkerPopOperations) {
    return new TimbuctooActions(
      mock(Authorizer.class),
      Clock.systemDefaultZone(),
      mock(PersistentUrlCreator.class),
      (coll, id, rev) -> URI.create("http://example.org/persistent"),
      tinkerPopOperations,
      new AfterSuccessTaskExecutor()
    );
  }

  public static TimbuctooActions withDataStoreAndAfterSucces(DataStoreOperations dataStoreOperations,
                                                             AfterSuccessTaskExecutor afterSuccessTaskExecutor) {
    return new TimbuctooActions(
      mock(Authorizer.class),
      Clock.systemDefaultZone(),
      mock(PersistentUrlCreator.class),
      (coll, id, rev) -> URI.create("http://example.org/persistent"),
      dataStoreOperations,
      afterSuccessTaskExecutor
    );
  }

  /**
   * Currently only used by {@link WomenWritersJsonCrudServiceTest}
   *
   * @deprecated mock TimbuctooActions or create one with one of the other stubs.
   */
  @Deprecated
  public static TimbuctooActions forGraphWrapper(TinkerPopGraphManager graphManager) {
    return new TimbuctooActions(
      mock(Authorizer.class),
      Clock.systemDefaultZone(),
      mock(PersistentUrlCreator.class),
      (coll, id, rev) -> URI.create("http://example.org/persistent"),
      TinkerPopOperationsStubs.forGraphWrapper(graphManager),
      new AfterSuccessTaskExecutor()
    );
  }


}
