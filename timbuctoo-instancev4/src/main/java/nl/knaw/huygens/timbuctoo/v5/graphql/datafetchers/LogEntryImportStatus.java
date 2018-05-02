package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.EntryImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;

import java.util.List;

public class LogEntryImportStatus {
  private final String id;
  private final String status;
  private final List<String> errors;
  private final String source;

  public LogEntryImportStatus(LogEntry logEntry, int id, boolean unprocessed) {
    this.id = "" + id;
    this.status = createStatus(logEntry, unprocessed);
    this.errors = logEntry.getImportStatus().getErrors();
    this.source = logEntry.getLogToken().orElse("");
  }

  public String getId() {
    return id;
  }

  private static String createStatus(LogEntry logEntry, boolean unprocessed) {
    boolean finished = logEntry.getImportStatus().getStatus().startsWith("Finished");

    return unprocessed ? "PENDING" : finished ? "DONE" : "IMPORTING";
  }

  public String getStatus() {
    return status;
  }

  public String getSource() {
    return source;
  }

  public List<String> getErrors() {
    return errors;
  }
}
