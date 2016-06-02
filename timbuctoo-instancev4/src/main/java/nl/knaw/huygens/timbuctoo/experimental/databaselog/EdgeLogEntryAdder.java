package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.model.Change;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import static java.util.stream.Collectors.toList;

public class EdgeLogEntryAdder {
  public static final Logger LOG = LoggerFactory.getLogger(EdgeLogEntryAdder.class);
  private final ObjectMapper objectMapper;
  private final TreeSet<EdgeLogEntry> edgeLogEntries;

  public EdgeLogEntryAdder() {
    edgeLogEntries = new TreeSet<>();
    objectMapper = new ObjectMapper();
  }

  public void appendEdgesToLog(DatabaseLog databaseLog, Long vertexTimeStamp) {
    List<EdgeLogEntry> edgesToAppend = edgeLogEntries.stream()
                                                     .filter(entry -> entry.getTimestamp() < vertexTimeStamp)
                                                     .collect(toList());
    edgesToAppend.forEach(entry -> entry.appendToLog(databaseLog));
    edgeLogEntries.removeAll(edgesToAppend);
  }

  public void entryFor(Edge edge) {
    Property<String> modifiedProp = edge.property("modified");
    String id = edge.value("tim_id");
    if (!modifiedProp.isPresent()) {
      logErrorForEdgeWithoutProp(id, "modified");
      return;
    }

    Property<Integer> rev = edge.property("rev");
    if (!rev.isPresent()) {
      logErrorForEdgeWithoutProp(id, "rev");
      return;
    }

    String modifiedString = modifiedProp.value();
    try {
      Change modified = objectMapper.readValue(modifiedString, Change.class);
      if (rev.value() > 1) {
        edgeLogEntries.add(new UpdateEdgeLogEntry(edge, modified.getTimeStamp(), id));
      } else {
        edgeLogEntries.add(new CreateEdgeLogEntry(edge, modified.getTimeStamp(), id));
      }
    } catch (IOException e) {
      LOG.error("String '{}' of Edge with id '{}' cannot be converted to Change", modifiedString, id);
      LOG.error("Exception thrown", e);
    }
  }

  private void logErrorForEdgeWithoutProp(String id, String propName) {
    LOG.error("Edge with id '{}' has no property '{}'. This edge will be ignored.", id, propName);
  }

  public void appendRemaining(DatabaseLog databaseLog) {
    edgeLogEntries.forEach(edgeLogEntry -> edgeLogEntry.appendToLog(databaseLog));
    edgeLogEntries.clear();
  }

  private static class CreateEdgeLogEntry extends EdgeLogEntry {
    private final Edge edge;

    public CreateEdgeLogEntry(Edge edge, Long timestamp, String id) {
      super(timestamp, id);
      this.edge = edge;
    }

    @Override
    public void appendToLog(DatabaseLog dbLog) {
      dbLog.newEdge(edge);
    }
  }

  private static class UpdateEdgeLogEntry extends EdgeLogEntry {
    private final Edge edge;

    public UpdateEdgeLogEntry(Edge edge, Long timestamp, String id) {
      super(timestamp, id);
      this.edge = edge;
    }

    @Override
    public void appendToLog(DatabaseLog dbLog) {
      dbLog.updateEdge(edge);
    }

  }
}
