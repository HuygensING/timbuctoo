package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.dto;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.LocalDataFile;

import java.util.ArrayList;
import java.util.List;

public class LogStorage {
  public List<LocalDataFile> getLogEntries() {
    return logEntries;
  }

  public void setLogEntries(List<LocalDataFile> logEntries) {
    this.logEntries = logEntries;
  }

  public List<LocalDataFile> logEntries = new ArrayList<>();
}
