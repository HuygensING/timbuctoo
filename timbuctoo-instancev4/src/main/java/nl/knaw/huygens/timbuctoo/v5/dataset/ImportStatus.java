package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Stopwatch;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogEntry;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.TimeWithUnit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME)
public class ImportStatus {

  private final LogList logList;
  private final Stopwatch stopwatch;

  private String methodName;
  private String baseUri;
  private int errorCount;
  private LogEntry currentLogEntry;
  private long currentEntryStart;

  public ImportStatus(LogList logList) {
    this.logList = logList;
    stopwatch = Stopwatch.createUnstarted();
  }

  public void start(String methodName, String baseUri) {
    reset();
    this.methodName = methodName;
    this.baseUri = baseUri;
    setStatus("Started " + methodName);
    stopwatch.start();
  }

  public long getElapsedTime(String unit) {
    return stopwatch.elapsed(TimeUnit.valueOf(unit));
  }

  public String getMethodName() {
    return methodName;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public String getStatus() {
    return logList.getLastStatus();
  }

  public void setStatus(String status) {
    logList.setLastStatus(status);
  }

  public void addListError(String message, Throwable error) {
    String errorString = "[" + Instant.now().toString() + "] " +
      "; method: " + methodName +
      "; message: " + message +
      "; error: " + error.getMessage();
    logList.addListError(errorString);
    errorCount += 1;
  }

  public List<String> getListErrors() {
    return logList.getListErrors();
  }

  public void setCurrentLogEntry(LogEntry logEntry) {
    currentLogEntry = logEntry;
    currentLogEntry.getImportStatus().ifPresent(eis -> {
      eis.setDate(Instant.now().toString());
    });
    currentEntryStart = stopwatch.elapsed(TimeUnit.MILLISECONDS);
  }

  public Optional<LogEntry> getCurrentLogEntry() {
    return Optional.ofNullable(currentLogEntry);
  }

  public void setEntryStatus(String entryStatus) {
    getCurrentLogEntry().ifPresent(entry -> {
      entry.getImportStatus().ifPresent(eis -> eis.setStatus(entryStatus));
    });
  }

  public void addEntryError(String message, Throwable error) {
    getCurrentLogEntry().ifPresent(entry -> {
      String errorString = "[" + Instant.now().toString() + "] " +
        "; message: " + message +
        "; error: " + error.getMessage();
      entry.getImportStatus().ifPresent(eis -> eis.addError(errorString));
    });
    logList.incrementEntityErrorCount();
    errorCount += 1;
  }

  public void finishEntry() {
    getCurrentLogEntry().ifPresent(entry -> {
      entry.getImportStatus().ifPresent(eis -> {
        int errors = eis.getErrors().size();
        eis.setStatus("Finished with " + errors + " error" + (errors == 1 ? "" : "s"));
        eis.setDate(Instant.now().toString());
        eis.setElapsedTimeMillis(stopwatch.elapsed(TimeUnit.MILLISECONDS) - currentEntryStart);
      });
    });
    currentLogEntry = null;
  }

  public void finishList() {
    stopwatch.stop();
    logList.setLastStatus("Finished import with " + errorCount + " error" + (errorCount == 1 ? "" : "s"));
    logList.setLastImportDate(Instant.now().toString());
    logList.setLastImportDuration(new TimeWithUnit(TimeUnit.MILLISECONDS.name(),
      stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    reset();
  }

  public boolean hasErrors() {
    return !logList.getListErrors().isEmpty() || logList.getEntityErrorCount() > 0;
  }

  private void reset() {
    errorCount = 0;
    methodName = null;
    baseUri = null;
    currentLogEntry = null;
    stopwatch.reset();
  }


  //////////////////////////////////////////////////////////

  private List<String> messages = new ArrayList<>();
  private String fatalError;



  public List<String> getMessages() {
    return messages;
  }


  public String getFatalError() {
    return fatalError;
  }




  @JsonIgnore
  public boolean isRunning() {
    return stopwatch.isRunning();
  }

  public void addMessage(String msg) {
    messages.add("[" + LocalDateTime.now().toString() + "] " + msg);
  }

  public void setFatalError(String msg, Throwable error) {
    setStopped();
    setStatus("Fatal Error");
    String message = msg == null ? "Fatal Error: " : msg + " :";
    this.fatalError = "[" + LocalDateTime.now().toString() + "] " + message + error.getMessage();
  }

  void setFinished() {
    setStopped();
  }


  private void setStopped() {
    if (stopwatch.isRunning()) {
      stopwatch.stop();
    }
  }

}
