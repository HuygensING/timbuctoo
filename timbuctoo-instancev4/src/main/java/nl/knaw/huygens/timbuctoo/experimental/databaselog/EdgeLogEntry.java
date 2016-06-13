package nl.knaw.huygens.timbuctoo.experimental.databaselog;

public abstract class EdgeLogEntry implements LogEntry {
  private final Long timestamp;
  private final String id;

  protected EdgeLogEntry(Long timestamp, String id) {
    this.timestamp = timestamp;
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public Long getTimestamp() {
    return timestamp;
  }

}
