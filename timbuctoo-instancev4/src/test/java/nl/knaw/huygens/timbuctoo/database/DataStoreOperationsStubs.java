package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.database.changelistener.ChangeListener;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.VertexDuplicator.VERSION_OF;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.mockito.Mockito.mock;

public class DataStoreOperationsStubs {
  public static DataStoreOperations forGraphWrapper(GraphWrapper graphWrapper) {
    return new DataStoreOperations(graphWrapper, mock(ChangeListener.class), new GremlinEntityFetcher(), null);
  }

  public static DataStoreOperations forChangeListenerMock(ChangeListener changeListener) {
    return new DataStoreOperations(newGraph().wrap(), changeListener, new GremlinEntityFetcher(), null);
  }

  public static DataStoreOperations forReplaceCall(ChangeListener changeListener, UUID id, int rev) {
    TinkerpopGraphManager wrap = newGraph()
      .withVertex("old", vertexBuilder -> vertexBuilder
        .withTimId(id.toString())
        .withProperty("rev", rev)
        .withProperty("isLatest", false)
      )
      .withVertex(vertexBuilder -> vertexBuilder
        .withTimId(id.toString())
        .withProperty("rev", rev)
        .withProperty("isLatest", true)
        .withIncomingRelation(VERSION_OF, "old")
      )
      .wrap();
    return new DataStoreOperations(wrap, changeListener, new GremlinEntityFetcher(), null);
  }

  public static DataStoreOperations forDeleteCall(ChangeListener changeListener, UUID id, int rev,
                                                   String entityTypeName) {
    TinkerpopGraphManager wrap = newGraph()
      .withVertex("old", vertexBuilder -> vertexBuilder
        .withTimId(id.toString())
        .withProperty("types", "[\"" + entityTypeName + "\"]")
        .withProperty("rev", rev)
        .withProperty("isLatest", false)
      )
      .withVertex(vertexBuilder -> vertexBuilder
        .withTimId(id.toString())
        .withProperty("types", "[\"" + entityTypeName + "\"]")
        .withProperty("rev", rev)
        .withProperty("isLatest", true)
        .withIncomingRelation(VERSION_OF, "old")
      )
      .wrap();
    return new DataStoreOperations(wrap, changeListener, new GremlinEntityFetcher(), null);
  }

}
