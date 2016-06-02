package nl.knaw.huygens.timbuctoo.experimental.databaselog;

public interface VertexLogEntry {
  void appendToLog(DatabaseLog dbLog);

  void addEdgeLogEntriesTo(EdgeLogEntryAdder edgeLogEntryAdder);
}
