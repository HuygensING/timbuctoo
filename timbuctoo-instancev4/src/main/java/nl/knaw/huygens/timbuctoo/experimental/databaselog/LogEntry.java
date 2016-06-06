package nl.knaw.huygens.timbuctoo.experimental.databaselog;

public interface LogEntry {
  void appendToLog(DatabaseLog dbLog);
}
