package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.EntryImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.ImportStatusLabel;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.v5.dataset.dto.ImportStatusLabel.DONE;
import static nl.knaw.huygens.timbuctoo.v5.dataset.dto.ImportStatusLabel.IMPORTING;
import static nl.knaw.huygens.timbuctoo.v5.dataset.dto.ImportStatusLabel.PENDING;

public class LogEntryImportStatus {
  private final String id;
  private final ImportStatusLabel status;
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

  private static ImportStatusLabel createStatus(LogEntry logEntry, boolean unprocessed) {
    EntryImportStatus importStatus = logEntry.getImportStatus();
    boolean hasDate = importStatus.getDate() != null; // means the import of the entry was started

    if (hasDate) {
      if (unprocessed) {
        return IMPORTING;
      } else {
        return DONE;
      }
    } else {
      return PENDING;
    }
  }

  public ImportStatusLabel getStatus() {
    return status;
  }

  public String getSource() {
    return source;
  }

  public List<String> getErrors() {
    return errors;
  }
}
