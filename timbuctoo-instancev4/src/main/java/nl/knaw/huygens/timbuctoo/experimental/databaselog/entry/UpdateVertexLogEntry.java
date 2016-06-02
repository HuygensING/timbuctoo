package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.EdgeLogEntryAdder;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.VertexLogEntry;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.Objects;
import java.util.Set;

public class UpdateVertexLogEntry implements VertexLogEntry {
  private final Vertex vertex;
  private final Vertex previous;


  public UpdateVertexLogEntry(Vertex vertex, Vertex previousVersion) {
    this.vertex = vertex;
    this.previous = previousVersion;
  }

  @Override
  public void appendToLog(DatabaseLog dbLog) {
    dbLog.updateVertex(vertex);

    Set<String> newKeys = vertex.keys();
    Set<String> oldKeys = previous.keys();
    addNewProperties(dbLog, newKeys, oldKeys);
    updateExistingProperties(dbLog, newKeys, oldKeys);
    removeDeletedProperties(dbLog, newKeys, oldKeys);
  }


  private void removeDeletedProperties(DatabaseLog dbLog, Set<String> newKeys, Set<String> oldKeys) {
    Set<String> deletedProperties = Sets.difference(oldKeys, newKeys);
    deletedProperties.forEach(property -> dbLog.deleteProperty(property));
  }

  private void updateExistingProperties(DatabaseLog dbLog, Set<String> newKeys, Set<String> oldKeys) {
    Set<String> existing = Sets.intersection(newKeys, oldKeys);
    existing.forEach(key -> {

      VertexProperty<Object> latestProperty = vertex.property(key);
      if (!Objects.equals(latestProperty.value(), previous.value(key))) {
        dbLog.updateProperty(latestProperty);
      }
    });
  }

  private void addNewProperties(DatabaseLog dbLog, Set<String> newKeys, Set<String> oldKeys) {
    Set<String> newProperties = Sets.difference(newKeys, oldKeys);
    newProperties.forEach(key -> dbLog.newProperty(vertex.property(key)));
  }

  @Override
  public void addEdgeLogEntriesTo(EdgeLogEntryAdder edgeLogEntryAdder) {
    /*
     * Do not add EdgeLogEntries. The CreateVertexLogEntry will add all the EdgeLogEntries tot he EdgeLogEntryAdder.
     * The EdgeLogEntryAdder will determine when the EdgeLogEntries should be added to the DatabaseLog.
     */

  }
}
