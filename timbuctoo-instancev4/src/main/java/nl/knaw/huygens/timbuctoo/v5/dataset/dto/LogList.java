package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class LogList {

  @JsonProperty
  private int processedUntil = -1;

  @JsonProperty
  private List<LogEntry> logEntries = new ArrayList<>();

  public int addEntry(LogEntry entry) {
    logEntries.add(entry);
    return logEntries.size() - 1;
  }

  @JsonIgnore
  public ListIterator<LogEntry> getUnprocessed() {
    return logEntries.listIterator(processedUntil + 1);
  }

  public void markAsProcessed(int marker) {
    this.processedUntil = marker;
  }

  @JsonIgnore
  public List<LogEntry> getEntries() {
    return logEntries;
  }
}
