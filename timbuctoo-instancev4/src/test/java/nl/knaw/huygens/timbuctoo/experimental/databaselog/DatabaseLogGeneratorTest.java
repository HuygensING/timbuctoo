package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import nl.knaw.huygens.timbuctoo.experimental.databaselog.entry.LogEntryFactory;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.VertexBuilder;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.shaded.jackson.core.JsonProcessingException;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class DatabaseLogGeneratorTest {

  private ObjectMapper objectMapper;
  private LogEntryFactory logEntryFactory;
  private VertexLogEntry vertexLogEntry;

  @Before
  public void setUp() throws Exception {
    objectMapper = new ObjectMapper();
    logEntryFactory = mock(LogEntryFactory.class);
    vertexLogEntry = mock(VertexLogEntry.class);
    given(logEntryFactory.createForVertex(any(Vertex.class))).willReturn(vertexLogEntry);
  }

  @Test
  public void generateIteratesThroughAllVerticesOrderedByModifiedDate() throws Exception {
    String first = "first";
    String second = "second";
    String third = "third";
    GraphWrapper graphWrapper = newGraph().withVertex(vertexWithIdAndModifiedTimeStamp(first, 1464346423))
                                          .withVertex(vertexWithIdAndModifiedTimeStamp(third, 1464346425))
                                          .withVertex(vertexWithIdAndModifiedTimeStamp(second, 1464346424))
                                          .wrap();
    DatabaseLogGenerator instance = new DatabaseLogGenerator(graphWrapper, logEntryFactory);

    instance.generate();

    InOrder inOrder = inOrder(logEntryFactory);
    inOrder.verify(logEntryFactory).createForVertex(argThat(likeVertex().withTimId(first)));
    inOrder.verify(logEntryFactory).createForVertex(argThat(likeVertex().withTimId(second)));
    inOrder.verify(logEntryFactory).createForVertex(argThat(likeVertex().withTimId(third)));
  }

  private Consumer<VertexBuilder> vertexWithIdAndModifiedTimeStamp(String id, long timeStamp) {
    try {
      String modifiedString = objectMapper.writeValueAsString(changeWithTimestamp(timeStamp));
      return v -> v.withTimId(id).withProperty("modified", modifiedString);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private Change changeWithTimestamp(long timeStamp) {
    return new Change(timeStamp, "", "");
  }

  @Test
  public void generateAppendsAVertexEntryForEachVertexToTheLog() throws Exception {
    GraphWrapper graphWrapper = newGraph().withVertex(vertexWithIdAndModifiedTimeStamp("id1", 1464346423))
                                          .withVertex(vertexWithIdAndModifiedTimeStamp("id2", 1464346425))
                                          .withVertex(vertexWithIdAndModifiedTimeStamp("id3", 1464346424))
                                          .wrap();
    DatabaseLogGenerator instance = new DatabaseLogGenerator(graphWrapper, logEntryFactory);

    instance.generate();

    verify(vertexLogEntry, times(3)).appendToLog(any(DatabaseLog.class));
  }


}
