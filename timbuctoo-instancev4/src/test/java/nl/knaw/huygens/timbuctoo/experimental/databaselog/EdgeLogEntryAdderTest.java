package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import nl.knaw.huygens.timbuctoo.model.Change;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static nl.knaw.huygens.timbuctoo.util.EdgeMatcher.likeEdge;
import static nl.knaw.huygens.timbuctoo.util.EdgeMockBuilder.edge;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class EdgeLogEntryAdderTest {

  private EdgeLogEntryAdder instance;
  private DatabaseLog databaseLog;

  @Before
  public void setUp() throws Exception {
    instance = new EdgeLogEntryAdder();
    databaseLog = mock(DatabaseLog.class);
  }

  @Test
  public void appendEdgesToLogAppendsOnlyTheEdgesThatHaveAModifiedTimestampSmallerThanTheInsertedModifiedTimestamp() {
    addEdgeWithModifiedTimestampAndIdAndRev(instance, 100000L, "id1", 1);
    addEdgeWithModifiedTimestampAndIdAndRev(instance, 100000L, "id2", 1);
    addEdgeWithModifiedTimestampAndIdAndRev(instance, 150000L, "id3", 1);
    addEdgeWithModifiedTimestampAndIdAndRev(instance, 200000L, "id4", 1);
    addEdgeWithModifiedTimestampAndIdAndRev(instance, 300000L, "id5", 1);

    instance.appendEdgesToLog(databaseLog, 200000L);

    verify(databaseLog, times(3)).newEdge(any(Edge.class));
  }

  @Test
  public void addendEdgesToLogAppendsTheEdgesOnceToTheDatabaseLog() {
    addEdgeWithModifiedTimestampAndIdAndRev(instance, 100000L, "id1", 1);
    instance.appendEdgesToLog(databaseLog, 200000L); // first invocation

    instance.appendEdgesToLog(databaseLog, 200000L); // second invocation

    verify(databaseLog, times(1)).newEdge(any(Edge.class));
  }

  @Test
  public void appendEdgesAppendsTheEdgesInOrder() {
    addEdgeWithModifiedTimestampAndIdAndRev(instance, 120000L, "id2", 1);
    addEdgeWithModifiedTimestampAndIdAndRev(instance, 100000L, "id1", 1);
    addEdgeWithModifiedTimestampAndIdAndRev(instance, 150000L, "id3", 1);

    instance.appendEdgesToLog(databaseLog, 200000L);

    InOrder inOrder = inOrder(databaseLog);
    inOrder.verify(databaseLog).newEdge(argThat(likeEdge().withModifiedTimestamp(100000L)));
    inOrder.verify(databaseLog).newEdge(argThat(likeEdge().withModifiedTimestamp(120000L)));
    inOrder.verify(databaseLog).newEdge(argThat(likeEdge().withModifiedTimestamp(150000L)));
  }

  @Test
  public void appendWillAppendAnUpdateEdgeLogMessageIfTheEdgeIsNotTheFirstRevision() {
    addEdgeWithModifiedTimestampAndIdAndRev(instance, 120000L, "id2", 2);

    instance.appendEdgesToLog(databaseLog, 200000L);

    verify(databaseLog).updateEdge(any(Edge.class));
  }

  private void addEdgeWithModifiedTimestampAndIdAndRev(EdgeLogEntryAdder instance, long timeStamp, String id, int rev) {
    Edge edge = edge().withProperty("modified", changeWithTimestamp(timeStamp))
                      .withProperty("tim_id", id)
                      .withProperty("rev", rev).build();
    instance.entryFor(edge);
  }

  private Change changeWithTimestamp(long timeStamp) {
    return new Change(timeStamp, "", "");
  }

}
