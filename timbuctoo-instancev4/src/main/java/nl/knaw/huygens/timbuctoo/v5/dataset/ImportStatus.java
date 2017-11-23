package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME)
public class ImportStatus {

  private static final Logger LOG = getLogger(ImportStatus.class);
  private static Map<Long, ImportStatus> statusMap = new ConcurrentHashMap<>();

  public static ImportStatus get() {
    long id = Thread.currentThread().getId();
    ImportStatus currentStatus = statusMap.get(id);
    if (currentStatus == null) {
      currentStatus = new ImportStatus();
      statusMap.put(id, currentStatus);
      LOG.warn("Getting import status and no status set: thread #" + id);
    }
    LOG.debug("Getting current import status for thread #{}", id );
    return currentStatus;
  }

  public static void set(ImportStatus currentStatus) {
    long id = Thread.currentThread().getId();
    currentStatus.setId(id);
    statusMap.put(id, currentStatus);
    LOG.debug("Setting current import status for thread #{}", id );
  }

  public static ImportStatus remove() {
    long id = Thread.currentThread().getId();
    ImportStatus currentStatus = statusMap.remove(id);
    LOG.debug("Removing current import status for thread #{}", id );
    return currentStatus;
  }

  private long id;
  private String methodName = "Unknown";
  private String baseUri;
  private String status = "Unknown";
  private long totalTime;
  private String elapsedTime;
  private List<String> messages = new ArrayList<>();
  private List<String> errors = new ArrayList<>();
  private String fatalError;
  private Stopwatch stopwatch = Stopwatch.createUnstarted();

  private void reset() {
    id = 0;
    methodName = "Unknown";
    baseUri = null;
    status = "Unknown";
    totalTime = 0L;
    messages.clear();
    errors.clear();
    fatalError = null;
    stopwatch.reset();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

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
    elapsedTime = stopwatch.isRunning() ? stopwatch.elapsed(TimeUnit.SECONDS) + " seconds" : null;
    return elapsedTime;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getTotalTime() {
    return totalTime == 0 ? null : totalTime + " seconds";
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

  void setStatus(String status) {
    this.status = "[" + LocalDateTime.now().toString() + "] " + status;
  }

  public void addMessage(String msg) {
    messages.add("[" + LocalDateTime.now().toString() + "] " + msg);
  }

  public void addError(String msg, Throwable error) {
    String message = msg == null ? "Error: " : msg + " :";
    errors.add("[" + LocalDateTime.now().toString() + "] " + message + error.getMessage());
  }

  public void setFatalError(String msg, Throwable error) {
    if (stopwatch.isRunning()) {
      stopwatch.stop();
    }
    totalTime = stopwatch.elapsed(TimeUnit.SECONDS);
    setStatus("Fatal Error");
    String message = msg == null ? "Fatal Error: " : msg + " :";
    this.fatalError = "[" + LocalDateTime.now().toString() + "] " + message + error.getMessage();
  }

  void setFinished() {
    if (stopwatch.isRunning()) {
      stopwatch.stop();
    }
    totalTime = stopwatch.elapsed(TimeUnit.SECONDS);
    setStatus("Finished");
  }

}
