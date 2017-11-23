package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
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
  private String totalTime;

  public String getStatus() {
    return status;
  }

  public String getMethodName() {
    return methodName;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getBaseUri() {
    return baseUri;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<String> getMessages() {
    return messages;
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<String> getErrors() {
    return errors;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getFatalError() {
    return fatalError;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getElapsedTime() {
    return stopwatch.isRunning() ? stopwatch.elapsed(TimeUnit.SECONDS) + " seconds" : null;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getTotalTime() {
    return totalTime;
  }

  public boolean hasErrors() {
    return !errors.isEmpty() || fatalError != null;
  }

  void setStarted(String methodName, String baseUri) {
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
    totalTime = null;
  }

  private void setStopped() {
    totalTime = stopwatch.elapsed(TimeUnit.SECONDS) + " seconds";
    if (stopwatch.isRunning()) {
      stopwatch.stop();
    }
  }

}
