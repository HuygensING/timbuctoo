package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import static java.util.stream.Collectors.toList;

public class EdgeLogEntryAdder {
  public static final Logger LOG = LoggerFactory.getLogger(EdgeLogEntryAdder.class);
  private final TreeSet<EdgeLogEntry> edgeLogEntries;

  public EdgeLogEntryAdder() {
    edgeLogEntries = new TreeSet<>(new EdgeLogEntryComparator());
  }

  public void appendEdgesToLog(DatabaseLog databaseLog, Long vertexTimeStamp) {
    List<EdgeLogEntry> edgesToAppend = edgeLogEntries.stream()
                                                     .filter(entry -> entry.getTimestamp() < vertexTimeStamp)
                                                     .collect(toList());
    edgesToAppend.forEach(entry -> entry.appendToLog(databaseLog));
    edgeLogEntries.removeAll(edgesToAppend);
  }

  public void entryFor(EdgeLogEntry edgeLogEntry) {
    this.edgeLogEntries.add(edgeLogEntry);
  }


  public void appendRemaining(DatabaseLog databaseLog) {
    edgeLogEntries.forEach(edgeLogEntry -> edgeLogEntry.appendToLog(databaseLog));
    edgeLogEntries.clear();
  }

  private static class EdgeLogEntryComparator implements Comparator<EdgeLogEntry> {
    @Override
    public int compare(EdgeLogEntry edgeLogEntry1, EdgeLogEntry edgeLogEntry2) {
      int timestampCompare = Long.compare(edgeLogEntry1.getTimestamp(), edgeLogEntry2.getTimestamp());

      return timestampCompare == 0 ? edgeLogEntry1.getId().compareTo(edgeLogEntry2.getId()) : timestampCompare;
    }
  }
}
