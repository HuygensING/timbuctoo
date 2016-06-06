package nl.knaw.huygens.timbuctoo.experimental.databaselog;

public abstract class EdgeLogEntry implements Comparable<EdgeLogEntry>, LogEntry{
  private final Long timestamp;
  private final String id;

  protected EdgeLogEntry(Long timestamp, String id) {
    this.timestamp = timestamp;
    this.id = id;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  @Override
  public int compareTo(EdgeLogEntry other) {
    int timeStampCompare = Long.compare(getTimestamp(), other.getTimestamp());
    return timeStampCompare == 0 ? id.compareTo(other.id) : timeStampCompare;
  }
}
