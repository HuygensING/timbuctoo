package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntry;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import nl.knaw.huygens.timbuctoo.model.Change;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.IOException;
import java.util.Iterator;

public class LogEntryFactory {

  private final EdgeRetriever edgeRetriever;

  public LogEntryFactory() {
    edgeRetriever = new EdgeRetriever();
  }

  public VertexLogEntry createForVertex(Vertex vertex) {
    Iterator<Vertex> previous = vertex.vertices(Direction.IN, "VERSION_OF");
    if (previous.hasNext()) {
      return new UpdateVertexLogEntry(vertex, previous.next());
    }
    return new CreateVertexLogEntry(vertex, this);
  }

  private Edge getPreviousVersion(Edge edge) {
    return edgeRetriever.getPreviousVersion(edge);
  }

  public EdgeLogEntry createForEdge(Edge edge) throws IllegalArgumentException {
    String id = edge.value("tim_id");
    Property<String> modifiedProp = edge.property("modified");
    if (!modifiedProp.isPresent()) {
      throw new IllegalArgumentException(messageForEdgeWithoutProp(id, "modified"));
    }
    String modifiedString = modifiedProp.value();

    Property<Integer> revProp = edge.property("rev");
    if (!revProp.isPresent()) {
      throw new IllegalArgumentException(messageForEdgeWithoutProp(id, "rev"));
    }
    Integer rev = revProp.value();

    try {
      Change modified = new ObjectMapper().readValue(modifiedString, Change.class);
      long modifiedTimeStamp = modified.getTimeStamp();

      if (rev > 1) {
        Edge prevEdge = getPreviousVersion(edge);

        return new UpdateEdgeLogEntry(edge, modifiedTimeStamp, id, prevEdge);
      }
      return new CreateEdgeLogEntry(edge, modifiedTimeStamp, id);
    } catch (IOException e) {
      throw new IllegalArgumentException(
        String.format("String '%s' of Edge with id '%s' cannot be converted to Change. Edge will be ignored.",
          modifiedString, id),
        e);
    }
  }

  private String messageForEdgeWithoutProp(String id, String propName) {
    return String.format("Edge with id '%s' has no property '%s'. This edge will be ignored.", id, propName);
  }

}
