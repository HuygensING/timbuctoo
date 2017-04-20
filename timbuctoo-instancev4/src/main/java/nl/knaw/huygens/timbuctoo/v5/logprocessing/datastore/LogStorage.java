package nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.dto.LocalLog;

public interface LogStorage {
  QuadHandler startWritingToLog(LocalLog log);

  void writeFinished(String dataSet);
}
