package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.LocalData;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.LocalDataFile;

import java.util.ArrayList;
import java.util.List;

public class LogStorage {
  private List<LocalData> logEntries = new ArrayList<>();

  public List<LocalData> getLogEntries() {
    return logEntries;
  }

  @JsonDeserialize(contentAs = LocalDataFile.class)
  public void setLogEntries(List<LocalData> logEntries) {
    this.logEntries = logEntries;
  }

}
