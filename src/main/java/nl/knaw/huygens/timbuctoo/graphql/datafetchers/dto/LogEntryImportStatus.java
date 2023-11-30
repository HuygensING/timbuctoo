package nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto;


import nl.knaw.huygens.timbuctoo.dataset.dto.EntryImportStatus;
import nl.knaw.huygens.timbuctoo.dataset.dto.ImportStatusLabel;
import nl.knaw.huygens.timbuctoo.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.dataset.dto.ProgressItem;

import java.util.List;
import java.util.stream.Collectors;

public class LogEntryImportStatus {
  private final String id;
  private final ImportStatusLabel status;
  private final List<String> errors;
  private final String source;
  private final List<ProgressStep> progress;
  private final List<ErrorObject> errorObjects;

  public LogEntryImportStatus(LogEntry logEntry, int id, boolean unprocessed) {
    this.id = "" + id;
    this.status = createStatus(logEntry, unprocessed);
    this.errors = logEntry.getImportStatus().getErrors();
    this.source = logEntry.getLogToken().orElse("");
    this.progress = createProgress(logEntry);
    this.errorObjects = errors.stream().map(ErrorObject::parse).collect(Collectors.toList());
  }

  private static ImportStatusLabel createStatus(LogEntry logEntry, boolean unprocessed) {
    EntryImportStatus importStatus = logEntry.getImportStatus();
    boolean hasDate = importStatus.getDate() != null; // means the import of the entry was started

    if (hasDate) {
      if (unprocessed) {
        return ImportStatusLabel.IMPORTING;
      } else {
        return ImportStatusLabel.DONE;
      }
    } else {
      return ImportStatusLabel.PENDING;
    }
  }

  private List<ProgressStep> createProgress(LogEntry logEntry) {
    return logEntry.getImportStatus().getProgressItems().entrySet().stream()
                   .filter(e -> e.getValue().getStatus() != ImportStatusLabel.DONE)
                   .map(e -> {
                     ProgressItem value = e.getValue();
                     return ProgressStep.create(e.getKey(), value.getStatus(), value.getProgress(), value.getSpeed());
                   }).collect(Collectors.toList());
  }

  public String getId() {
    return id;
  }

  public ImportStatusLabel getStatus() {
    return status;
  }

  public String getSource() {
    return source;
  }

  /**
   * @deprecated deprecated in the GraphQL schema
   */
  @Deprecated
  public List<String> getErrors() {
    return errors;
  }

  public List<ErrorObject> getErrorObjects() {
    return errorObjects;
  }

  public List<ProgressStep> getProgress() {
    return progress;
  }

}
