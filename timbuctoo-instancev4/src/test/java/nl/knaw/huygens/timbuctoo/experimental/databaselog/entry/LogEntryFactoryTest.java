package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntry;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
import static nl.knaw.huygens.timbuctoo.util.EdgeMockBuilder.edge;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class LogEntryFactoryTest {
  @Test
  public void createForVertexCreatesACreateVertexLogEntry() {
    Vertex vertex = vertex().build();
    LogEntryFactory instance = new LogEntryFactory();

    VertexLogEntry entry = instance.createForVertex(vertex);

    assertThat(entry, is(instanceOf(CreateVertexLogEntry.class)));
  }

  @Test
  public void createForVertexCreatesAnUpdateVertexLogEntryIfTheVertexHasIncomingVersionOfEdges() {
    Vertex vertex = vertex().withIncomingRelation("VERSION_OF", vertex().build()).build();
    LogEntryFactory instance = new LogEntryFactory();

    VertexLogEntry entry = instance.createForVertex(vertex);

    assertThat(entry, is(instanceOf(UpdateVertexLogEntry.class)));
  }

  @Test
  public void createForEdgeCreatesACreateEdgeLogEntry() {
    Edge edge = edge().withProperty("rev", 1).build();
    LogEntryFactory instance = new LogEntryFactory();

    EdgeLogEntry entry = instance.createForEdge(edge, 1000L, "id");

    assertThat(entry, is(instanceOf(CreateEdgeLogEntry.class)));
  }

  @Test
  public void createForEdgeCreatesAnUpdateEdgeLogEntryWhenTheEdgeHasARevHigherThan1() {
    UUID edgeId = UUID.randomUUID();
    Graph graph = newGraph().withVertex("v1", v -> v.withTimId("id1"))
                            .withVertex("v2",
                              v -> v.withOutgoingRelation("edge", "v1", e -> e.withRev(1).withTim_id(edgeId))
                                    .withOutgoingRelation("edge", "v1", e -> e.withRev(2).withTim_id(edgeId)))
                            .build();

    Edge edge = graph.traversal().E().has("tim_id", edgeId.toString()).has("rev", 2).next();

    LogEntryFactory instance = new LogEntryFactory();

    EdgeLogEntry entry = instance.createForEdge(edge, 1000L, edgeId.toString());

    assertThat(entry, is(instanceOf(UpdateEdgeLogEntry.class)));
  }

}
