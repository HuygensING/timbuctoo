package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.dto;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.LocalData;

import java.util.ArrayList;
import java.util.List;

public class LogStorage {
  public List<LocalData> logEntries = new ArrayList<>();

  public List<LocalData> getLogEntries() {
    return logEntries;
  }

  public void setLogEntries(List<LocalData> logEntries) {
    this.logEntries = logEntries;
  }

}
