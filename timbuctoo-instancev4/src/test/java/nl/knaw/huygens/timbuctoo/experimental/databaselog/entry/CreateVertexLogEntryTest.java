package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntryAdder;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.mockito.InOrder;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static nl.knaw.huygens.timbuctoo.util.EdgeMatcher.likeEdge;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexPropertyMatcher.likeVertexProperty;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.inE;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CreateVertexLogEntryTest {
  @Test
  public void appendToLogAddsNewVertexLineForAVertexWithoutPreviousVersions() {
    Vertex vertex = vertex().build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    VertexLogEntry instance = createInstance(vertex, mock(GraphWrapper.class));

    instance.appendToLog(dbLog);

    verify(dbLog).newVertex(vertex);
  }

  @Test
  public void appendToLogAddsANewPropertyLineForEachNewPropertyAfter() {
    String key1 = "property1";
    String key2 = "property2";
    String key3 = "property3";
    Vertex vertex = vertex()
      .withProperty(key1, "value")
      .withProperty(key2, "value")
      .withProperty(key3, "value")
      .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    CreateVertexLogEntry instance = createInstance(vertex, mock(GraphWrapper.class));

    instance.appendToLog(dbLog);

    verify(dbLog).newProperty(argThat(likeVertexProperty().withKey(key1)));
    verify(dbLog).newProperty(argThat(likeVertexProperty().withKey(key2)));
    verify(dbLog).newProperty(argThat(likeVertexProperty().withKey(key3)));
  }

  @Test
  public void appendLogFirstAddsNewVertexThanTheNewProperties() {
    String key1 = "property1";

    Vertex vertex = vertex()
      .withProperty(key1, "value")
      .build();
    DatabaseLog dbLog = mock(DatabaseLog.class);
    CreateVertexLogEntry vertexLogEntry = createInstance(vertex, mock(GraphWrapper.class));

    vertexLogEntry.appendToLog(dbLog);

    InOrder inOrder = inOrder(dbLog);
    inOrder.verify(dbLog).newVertex(vertex);
    inOrder.verify(dbLog).newProperty(argThat(likeVertexProperty().withKey(key1)));
  }


  @Test
  public void addEdgeLogEntriesToAddsAnEntryFromEachOutgoingEdgeToTheEdgeLogEntryAdder() {
    Graph graph = newGraph().withVertex("otherV1", v -> v.withTimId("o1"))
                                   .withVertex("otherV2", v -> v.withTimId("o2"))
                                   .withVertex("v3", v -> v.withTimId("id")
                                                           .withOutgoingRelation("otherRel", "otherV1")
                                                           .withIncomingRelation("otherRel2", "otherV2"))
                                   .withVertex("v2", v -> v.withTimId("id")
                                                           .withOutgoingRelation("VERSION_OF", "v3"))
                                   .withVertex("v1", v -> v.withTimId("id")
                                                           .withOutgoingRelation("VERSION_OF", "v1"))
                                   .build();
    Vertex vertex = graph.traversal().V().has("tim_id", "id").where(inE("VERSION_OF").count().is(0)).next();
    CreateVertexLogEntry instance = createInstance(vertex, mock(GraphWrapper.class));
    EdgeLogEntryAdder logEntryAdder = mock(EdgeLogEntryAdder.class);

    instance.addEdgeLogEntriesTo(logEntryAdder);

    verify(logEntryAdder).entryFor(argThat(likeEdge().withLabel("otherRel")));
    verifyNoMoreInteractions(logEntryAdder);
  }

  private CreateVertexLogEntry createInstance(Vertex vertex, GraphWrapper graphWrapper) {
    return new CreateVertexLogEntry(vertex);
  }

}
