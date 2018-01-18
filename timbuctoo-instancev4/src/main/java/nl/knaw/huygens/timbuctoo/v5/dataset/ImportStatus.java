package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * Record current status of import on a dataSet and provide lock mechanism for concurrent updates.
 * <p>
 * Import cycles follow steps of this pattern:
 * <ul>
 *   <li>{@link #lock(String, String)} start the import process; set a lock on further imports</li>
 *   <li>repeat 0 or more times:
 *    <ul>
 *      <li>{@link #startEntry(LogEntry)} start the import of one logEntry;</li>
 *      <li>{@link #finishEntry()} finish the import of one logEntry;</li>
 *    </ul>
 *   </li>
 *   <li>{@link #finishList()} finish the import process.</li>
 *   <li>{@link #unlock()} release the lock on imports</li>
 * </ul>
 * In between {@link #lock(String, String)} and {@link #finishList()} the methods {@link #setStatus(String)}
 * and {@link #addError(String, Throwable)} can be called.
 * The last status and all error messages will be persisted via {@link LogList} and {@link LogEntry} respectively.
 * The {@link #finishList()} method returns an {@link ImportStatusReport} on the finished import.
 * </p>
 */
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME)
public class ImportStatus {

  private static TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;

  private final LogList logList;
  private final Stopwatch stopwatch;
  private final ConcurrentLinkedDeque<String> messages;
  private final ConcurrentLinkedDeque<String> errors;

  private String methodName;
  private String baseUri;
  private String date;
  private Throwable lastError;

  private LogEntry currentLogEntry;
  private long currentEntryStart;

  private boolean locked;

  public ImportStatus(LogList logList) {
    this.logList = logList;
    stopwatch = Stopwatch.createUnstarted();
    messages = new ConcurrentLinkedDeque<>();
    errors = new ConcurrentLinkedDeque<>();
  }

  protected synchronized void lock(String methodName, String baseUri) {
    while (locked) {
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
    locked = true;
    reset();
    messages.clear();
    errors.clear();
    this.methodName = methodName;
    this.baseUri = baseUri;
    setStatus("Started " + this.methodName);
    stopwatch.start();
  }

  protected void unlock() {
    locked = false;
  }

  public void startEntry(LogEntry logEntry) {
    logEntry.getLogToken().ifPresent(token -> setStatus("Adding entry with token " + token));
    logEntry.getRdfCreator()
                   .ifPresent(creator -> setStatus("Creating entry with " + creator.getClass().getSimpleName()));
    currentLogEntry = logEntry;
    date = Instant.now().toString();
    currentLogEntry.getImportStatus().ifPresent(eis -> eis.setDate(date));
    currentEntryStart = stopwatch.elapsed(TIME_UNIT);
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
    lastError = error;
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
          new TimeWithUnit(TIME_UNIT, stopwatch.elapsed(TIME_UNIT) - currentEntryStart));
      });
      currentLogEntry = null;
      entry.getLogToken().ifPresent(token -> setStatus("Finished adding entry with token " + token));
      entry.getRdfCreator()
           .ifPresent(creator -> setStatus("Finished creating entry with " + creator.getClass().getSimpleName()));
    });
  }

  public ImportStatusReport finishList() {
    if (stopwatch.isRunning()) {
      stopwatch.stop();
      int errorCount = errors.size();
      setStatus("Finished import with " + errorCount + " error" + (errorCount == 1 ? "" : "s"));
      date = Instant.now().toString();
      logList.setLastImportDate(date);
      logList.setLastImportDuration(new TimeWithUnit(TIME_UNIT, stopwatch.elapsed(TIME_UNIT)));
    }
    return getImportStatusReport();
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

  @JsonIgnore
  public Throwable getLastError() {
    return lastError;
  }

  public long getElapsedTime(String unit) {
    if (messages.isEmpty()) {
      return -1L;
    } else {
      return stopwatch.elapsed(TimeUnit.valueOf(unit));
    }
  }

  public TimeWithUnit getElapsedTime() {
    return new TimeWithUnit(TIME_UNIT, getElapsedTime(TIME_UNIT.name()));
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
    lastError = null;
    currentLogEntry = null;
    stopwatch.reset();
  }

  private ImportStatusReport getImportStatusReport() {
    return ImmutableImportStatusReport.builder()
      .baseUri(getBaseUri())
      .status(getStatus())
      .messages(getMessages())
      .errors(getErrors())
      .date(getDate())
      .elapsedTime(getElapsedTime())
      .errorCount(getErrorCount())
      .hasErrors(hasErrors())
      .lastError(getLastError())
      .methodName(getMethodName())
      .build();
  }



}
