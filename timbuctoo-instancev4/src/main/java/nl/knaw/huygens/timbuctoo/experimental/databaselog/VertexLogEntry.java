package nl.knaw.huygens.timbuctoo.experimental.databaselog;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface VertexLogEntry {
  void appendToLog(DatabaseLog dbLog, Vertex vertex);
}
