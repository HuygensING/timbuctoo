package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import java.net.URI;
import java.time.Clock;

import static org.mockito.Mockito.mock;

public class TimbuctooActionsStubs {
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

  public static TimbuctooActions forGraphWrapper(GraphWrapper graphWrapper) {
    return new TimbuctooActions(
      mock(Authorizer.class),
      Clock.systemDefaultZone(),
      mock(PersistentUrlCreator.class),
      (coll, id, rev) -> URI.create("http://example.org/persistent"),
      new DataStoreOperations(graphWrapper, mock(ChangeListener.class), new GremlinEntityFetcher(), null),
      new AfterSuccessTaskExecutor()
    );
  }

}
