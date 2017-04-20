package nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.dto.LocalLog;

import java.net.URI;

public interface LogMetadata {
  void addLog(String dataSet, URI logUri);

  LocalLog startOrContinueAppendLog(String dataSet);

  void appendToLogFinished(String dataSet);
}
