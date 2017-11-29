package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Stopwatch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME)
public class ImportStatus {

  private String methodName = "Unknown";
  private String baseUri;
  private String status = "Unknown";
  private List<String> messages = new ArrayList<>();
  private List<String> errors = new ArrayList<>();
  private String fatalError;
  private Stopwatch stopwatch = Stopwatch.createUnstarted();
  private boolean started;

  public String getStatus() {
    return status;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public List<String> getMessages() {
    return messages;
  }

  public List<String> getErrors() {
    return errors;
  }

  public String getFatalError() {
    return fatalError;
  }

  public long getElapsedTime() {
    return getElapsedTime("SECONDS");
  }

  public long getElapsedTime(String unit) {
    return stopwatch.elapsed(TimeUnit.valueOf(unit));
  }

  public boolean hasErrors() {
    return !errors.isEmpty() || fatalError != null;
  }

  public boolean isStarted() {
    return started;
  }

  @JsonIgnore
  public boolean isRunning() {
    return stopwatch.isRunning();
  }

  void setStarted(String methodName, String baseUri) {
    started = true;
    reset();
    stopwatch.start();
    setStatus("Started");
    this.methodName = methodName;
    this.baseUri = baseUri;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void addMessage(String msg) {
    messages.add("[" + LocalDateTime.now().toString() + "] " + msg);
  }

  public void addError(String msg, Throwable error) {
    String message = msg == null ? "Error: " : msg + " :";
    errors.add("[" + LocalDateTime.now().toString() + "] " + message + error.getMessage());
  }

  public void setFatalError(String msg, Throwable error) {
    setStopped();
    setStatus("Fatal Error");
    String message = msg == null ? "Fatal Error: " : msg + " :";
    this.fatalError = "[" + LocalDateTime.now().toString() + "] " + message + error.getMessage();
  }

  void setFinished() {
    setStopped();
    setStatus("Finished with " + errors.size() + " errors");
  }

  private void reset() {
    methodName = "Unknown";
    baseUri = null;
    status = "Unknown";
    messages.clear();
    errors.clear();
    fatalError = null;
    stopwatch.reset();
  }

  private void setStopped() {
    if (stopwatch.isRunning()) {
      stopwatch.stop();
    }
  }

}
