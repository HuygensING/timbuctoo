package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    final EdgeLogEntry edgeLogEntry = addEdgeLogEntryWithTimestamp(instance, 100000L, "id");
    final EdgeLogEntry edgeLogEntry1 = addEdgeLogEntryWithTimestamp(instance, 100000L, "id1");
    final EdgeLogEntry edgeLogEntry2 = addEdgeLogEntryWithTimestamp(instance, 150000L, "id2");
    final EdgeLogEntry edgeLogEntry3 = addEdgeLogEntryWithTimestamp(instance, 200000L, "id3");
    final EdgeLogEntry edgeLogEntry4 = addEdgeLogEntryWithTimestamp(instance, 300000L, "id4");

    instance.appendEdgesToLog(databaseLog, 200000L);

    verify(edgeLogEntry).appendToLog(databaseLog);
    verify(edgeLogEntry1).appendToLog(databaseLog);
    verify(edgeLogEntry2).appendToLog(databaseLog);
    verify(edgeLogEntry3, never()).appendToLog(databaseLog);
    verify(edgeLogEntry4, never()).appendToLog(databaseLog);
  }

  @Test
  public void addendEdgesToLogAppendsTheEdgesOnceToTheDatabaseLog() {
    EdgeLogEntry edgeLogEntry = addEdgeLogEntryWithTimestamp(instance, 100000L, "id");
    instance.appendEdgesToLog(databaseLog, 200000L); // first invocation

    instance.appendEdgesToLog(databaseLog, 200000L); // second invocation

    verify(edgeLogEntry, timeout(1)).appendToLog(databaseLog);
  }

  @Test
  public void appendEdgesAppendsTheEdgesInOrder() {
    EdgeLogEntry edgeLogEntry = addEdgeLogEntryWithTimestamp(instance, 120000L, "id");
    EdgeLogEntry edgeLogEntry1 = addEdgeLogEntryWithTimestamp(instance, 100000L, "id1");
    EdgeLogEntry edgeLogEntry2 = addEdgeLogEntryWithTimestamp(instance, 150000L, "id2");

    instance.appendEdgesToLog(databaseLog, 200000L);

    InOrder inOrder = inOrder(edgeLogEntry, edgeLogEntry1, edgeLogEntry2);
    inOrder.verify(edgeLogEntry1).appendToLog(databaseLog);
    inOrder.verify(edgeLogEntry).appendToLog(databaseLog);
    inOrder.verify(edgeLogEntry2).appendToLog(databaseLog);
  }

  private EdgeLogEntry addEdgeLogEntryWithTimestamp(EdgeLogEntryAdder instance, long timeStamp, final String id) {
    EdgeLogEntry edgeLogEntry = mock(EdgeLogEntry.class);
    when(edgeLogEntry.getTimestamp()).thenReturn(timeStamp);
    when(edgeLogEntry.getId()).thenReturn(id);

    instance.entryFor(edgeLogEntry);

    return edgeLogEntry;
  }

}
