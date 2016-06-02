package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.search.MockVertexBuilder.vertex;
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

}
