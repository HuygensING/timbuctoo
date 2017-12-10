package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Stopwatch;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.TimeWithUnit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

/**
 * Record current status of import on a dataSet.
 * <p>
 * Import cycles follow steps of this pattern:
 * <ul>
 *   <li>{@link #start(String, String)} start the import process;</li>
 *   <li>repeat 0 or more times:
 *    <ul>
 *      <li>{@link #startEntry(LogEntry)} start the import of one logEntry;</li>
 *      <li>{@link #finishEntry()} finish the import of one logEntry;</li>
 *    </ul>
 *   </li>
 *   <li>{@link #finishList()} finish the import process.</li>
 * </ul>
 * In between all steps methods {@link #setStatus(String)} and {@link #addError(String, Throwable)} can be called.
 * The last status and all error messages will be persisted via {@link LogList} and {@link LogEntry} respectively.
 * </p>
 */
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME)
public class ImportStatus {

  private final LogList logList;
  private final Stopwatch stopwatch;
  private final ConcurrentLinkedDeque<String> messages;
  private final ConcurrentLinkedDeque<String> errors;

  private String methodName;
  private String baseUri;
  private String date;

  private LogEntry currentLogEntry;
  private long currentEntryStart;

  public ImportStatus(LogList logList) {
    this.logList = logList;
    stopwatch = Stopwatch.createUnstarted();
    messages = new ConcurrentLinkedDeque<>();
    errors = new ConcurrentLinkedDeque<>();
  }

  public void start(String methodName, String baseUri) {
    reset();
    messages.clear();
    errors.clear();
    this.methodName = methodName;
    this.baseUri = baseUri;
    setStatus("Started " + this.methodName);
    stopwatch.start();
  }

  public void startEntry(LogEntry logEntry) {
    logEntry.getLogToken().ifPresent(token -> setStatus("Adding entry with token " + token));
    logEntry.getRdfCreator()
                   .ifPresent(creator -> setStatus("Creating entry with " + creator.getClass().getSimpleName()));
    currentLogEntry = logEntry;
    date = Instant.now().toString();
    currentLogEntry.getImportStatus().ifPresent(eis -> eis.setDate(date));
    currentEntryStart = stopwatch.elapsed(TimeUnit.MILLISECONDS);
  }

  public void setStatus(String status) {
    if (currentLogEntry != null) {
      currentLogEntry.getImportStatus().ifPresent(eis -> eis.setStatus(status));
    } else {
      logList.setLastStatus(status);
    }
    messages.add(status);
  }

  public void addError(String message, Throwable error) {
    String errorString = "[" + Instant.now().toString() + "] " +
      "; method: " + methodName +
      "; message: " + message +
      "; error: " + error.getMessage();
    if (currentLogEntry != null) {
      currentLogEntry.getImportStatus().ifPresent(eis -> eis.addError(errorString));
    } else {
      logList.addListError(errorString);
    }
    errors.add(errorString);
    messages.add("ERROR: " + errorString);
  }

  public void finishEntry() {
    getCurrentLogEntry().ifPresent(entry -> {
      entry.getImportStatus().ifPresent(eis -> {
        int errorCount = eis.getErrors().size();
        setStatus("Finished entry with " + errorCount + " error" + (errorCount == 1 ? "" : "s"));
        date = Instant.now().toString();
        eis.setDate(date);
        eis.setElapsedTime(
          new TimeWithUnit().withMilliseconds(stopwatch.elapsed(TimeUnit.MILLISECONDS) - currentEntryStart));
      });
      currentLogEntry = null;
      entry.getLogToken().ifPresent(token -> setStatus("Finished adding entry with token " + token));
      entry.getRdfCreator()
           .ifPresent(creator -> setStatus("Finished creating entry with " + creator.getClass().getSimpleName()));
    });
  }

  public void finishList() {
    stopwatch.stop();
    int errorCount = errors.size();
    setStatus("Finished import with " + errorCount + " error" + (errorCount == 1 ? "" : "s"));
    date = Instant.now().toString();
    logList.setLastImportDate(date);
    logList.setLastImportDuration(new TimeWithUnit().withMilliseconds(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
  }

  public String getMethodName() {
    return methodName;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public String getStatus() {
    if (messages.isEmpty()) {
      return "No current import";
    } else {
      return messages.getLast();
    }
  }

  public String getDate() {
    return date;
  }

  public List<String> getMessages() {
    return new ArrayList<>(messages);
  }

  public List<String> getErrors() {
    return new ArrayList<>(errors);
  }

  public int getErrorCount() {
    return errors.size();
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public long getElapsedTime(String unit) {
    if (messages.isEmpty()) {
      return -1L;
    } else {
      return stopwatch.elapsed(TimeUnit.valueOf(unit));
    }
  }

  public TimeWithUnit getElapsedTime() {
    return new TimeWithUnit().withMilliseconds(getElapsedTime(TimeUnit.MILLISECONDS.name()));
  }

  public boolean isActive() {
    return stopwatch.isRunning();
  }

  private Optional<LogEntry> getCurrentLogEntry() {
    return Optional.ofNullable(currentLogEntry);
  }

  private void reset() {
    methodName = null;
    baseUri = null;
    currentLogEntry = null;
    stopwatch.reset();
  }

}
