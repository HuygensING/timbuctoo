package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.entry.LogEntryFactory;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.InOrder;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class DatabaseLogGeneratorTest {
  @Test
  public void generateIteratesThroughAllVerticesOrderedByModifiedDate() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String change1String = objectMapper.writeValueAsString(changeWithTimestamp(1464346423));
    String change2String = objectMapper.writeValueAsString(changeWithTimestamp(1464346424));
    String change3String = objectMapper.writeValueAsString(changeWithTimestamp(1464346425));
    String first = "first";
    String second = "second";
    String third = "third";
    GraphWrapper graph = newGraph().withVertex(v -> v.withTimId(first).withProperty("modified", change1String))
                                   .withVertex(v -> v.withTimId(third).withProperty("modified", change3String))
                                   .withVertex(v -> v.withTimId(second).withProperty("modified", change2String))
                                   .wrap();
    VertexLogEntry vertexLogEntry = mock(VertexLogEntry.class);
    LogEntryFactory logEntryFactory = mock(LogEntryFactory.class);
    given(logEntryFactory.createForVertex(any(Vertex.class))).willReturn(vertexLogEntry);
    DatabaseLogGenerator instance = new DatabaseLogGenerator(graph, logEntryFactory);

    instance.generate();

    InOrder inOrder = inOrder(vertexLogEntry);
    inOrder.verify(vertexLogEntry).appendToLog(any(DatabaseLog.class), argThat(likeVertex().withTimId(first)));
    inOrder.verify(vertexLogEntry).appendToLog(any(DatabaseLog.class), argThat(likeVertex().withTimId(second)));
    inOrder.verify(vertexLogEntry).appendToLog(any(DatabaseLog.class), argThat(likeVertex().withTimId(third)));
  }

  private Change changeWithTimestamp(int timeStamp) {
    return new Change(timeStamp, "", "");
  }

}
