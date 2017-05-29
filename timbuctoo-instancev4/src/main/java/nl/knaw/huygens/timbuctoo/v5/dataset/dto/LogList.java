package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class LogList {

  protected int processedUntil = 0;
  private List<LogEntry> logEntries = new ArrayList<>();

  public int addEntry(LogEntry entry) {
    logEntries.add(entry);
    return logEntries.size() - 1;
  }

  public ListIterator<LogEntry> getUnprocessed() {
    return logEntries.listIterator(processedUntil);
  }

  public void markAsProcessed(int marker) {
    this.processedUntil = marker;
  }

  public List<LogEntry> getEntries() {
    return logEntries;
  }
}
