package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntry;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;

import java.util.Objects;
import java.util.Set;

class UpdateEdgeLogEntry extends EdgeLogEntry {
  private final Edge edge;
  private final Edge prevEdge;

  public UpdateEdgeLogEntry(Edge edge, Long timestamp, String id, Edge prevEdge) {
    super(timestamp, id);
    this.edge = edge;
    this.prevEdge = prevEdge;
  }

  @Override
  public void appendToLog(DatabaseLog dbLog) {
    dbLog.updateEdge(edge);

    Set<String> newKeys = edge.keys();
    Set<String> oldKeys = prevEdge.keys();

    addNewProperties(dbLog, newKeys, oldKeys);
    updateExistingProperties(dbLog, newKeys, oldKeys);
    removeDeletedProperties(dbLog, newKeys, oldKeys);
  }

  private void addNewProperties(DatabaseLog dbLog, Set<String> newKeys, Set<String> oldKeys) {
    Set<String> newProperties = Sets.difference(newKeys, oldKeys);
    newProperties.forEach(key -> dbLog.newProperty(edge.property(key)));
  }

  private void updateExistingProperties(DatabaseLog dbLog, Set<String> newKeys, Set<String> oldKeys) {
    Set<String> existing = Sets.intersection(newKeys, oldKeys);
    existing.forEach(key -> {
      Property<Object> latestProperty = edge.property(key);
      if (!Objects.equals(latestProperty.value(), prevEdge.value(key))) {
        dbLog.updateProperty(latestProperty);
      }
    });
  }

  private void removeDeletedProperties(DatabaseLog dbLog, Set<String> newKeys, Set<String> oldKeys) {
    Set<String> deletedProperties = Sets.difference(oldKeys, newKeys);
    deletedProperties.forEach(property -> dbLog.deleteProperty(property));
  }

}
