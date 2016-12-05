package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.changelistener.ChangeListener;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.tinkerpop.VertexDuplicator.VERSION_OF;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.mockito.Mockito.mock;

public class TinkerPopOperationsStubs {
  public static TinkerPopOperations forGraphWrapper(TinkerPopGraphManager graphManager) {

    return new TinkerPopOperations(graphManager, mock(ChangeListener.class), new GremlinEntityFetcher(), null,
      mock(IndexHandler.class));
  }

  public static TinkerPopOperations forChangeListenerMock(ChangeListener changeListener) {
    return new TinkerPopOperations(newGraph().wrap(), changeListener, new GremlinEntityFetcher(), null,
      mock(IndexHandler.class));
  }

  public static TinkerPopOperations forReplaceCall(ChangeListener changeListener, UUID id, int rev) {
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
    return new TinkerPopOperations(wrap, changeListener, new GremlinEntityFetcher(), null, mock(IndexHandler.class));
  }

  public static TinkerPopOperations forDeleteCall(ChangeListener changeListener, UUID id, int rev,
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
    return new TinkerPopOperations(wrap, changeListener, new GremlinEntityFetcher(), null, mock(IndexHandler.class));
  }

  public static TinkerPopOperations forGraphWrapperAndMappings(TinkerPopGraphManager graphManager, Vres mappings) {
    return new TinkerPopOperations(graphManager, mock(ChangeListener.class), new GremlinEntityFetcher(), mappings,
      mock(IndexHandler.class));
  }
}
