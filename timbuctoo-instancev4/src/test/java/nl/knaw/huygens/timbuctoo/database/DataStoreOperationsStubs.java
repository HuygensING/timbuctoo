package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.database.changelistener.ChangeListener;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;

import static org.mockito.Mockito.mock;

public class DataStoreOperationsStubs {
  public static DataStoreOperations forGraphWrapper(GraphWrapper graphWrapper) {
    return new DataStoreOperations(graphWrapper, mock(ChangeListener.class), new GremlinEntityFetcher(), null);
  }
}
