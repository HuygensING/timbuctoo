package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.database.changelistener.ChangeListener;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.IndexHandler;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.VertexDuplicator.VERSION_OF;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.mockito.Mockito.mock;

public class DataStoreOperationsStubs {
  public static DataStoreOperations forGraphWrapper(TinkerPopGraphManager graphManager) {

    return new DataStoreOperations(graphManager, mock(ChangeListener.class), new GremlinEntityFetcher(), null,
      mock(IndexHandler.class));
  }

  public static DataStoreOperations forChangeListenerMock(ChangeListener changeListener) {
    return new DataStoreOperations(newGraph().wrap(), changeListener, new GremlinEntityFetcher(), null,
      mock(IndexHandler.class));
  }

  public static DataStoreOperations forReplaceCall(ChangeListener changeListener, UUID id, int rev) {
    TinkerPopGraphManager wrap = newGraph()
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
    return new DataStoreOperations(wrap, changeListener, new GremlinEntityFetcher(), null, mock(IndexHandler.class));
  }

  public static DataStoreOperations forDeleteCall(ChangeListener changeListener, UUID id, int rev,
                                                  String entityTypeName) {
    TinkerPopGraphManager wrap = newGraph()
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
    return new DataStoreOperations(wrap, changeListener, new GremlinEntityFetcher(), null, mock(IndexHandler.class));
  }

  public static DataStoreOperations forGraphWrapperAndMappings(TinkerPopGraphManager graphManager, Vres mappings) {
    return new DataStoreOperations(graphManager, mock(ChangeListener.class), new GremlinEntityFetcher(), mappings,
      mock(IndexHandler.class));
  }
}
