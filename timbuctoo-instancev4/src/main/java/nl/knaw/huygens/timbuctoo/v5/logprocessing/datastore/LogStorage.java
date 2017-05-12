package nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.DataSetLogEntry;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.LocalData;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.RdfCreator;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;

import java.net.URI;

public interface LogStorage {
  LocalData getLog(URI logUri) throws LogStorageFailedException;

  void addLog(URI logUri, LocalData logEntry) throws LogStorageFailedException;

  LocalData getCurrentAppendLog(RdfCreator generator) throws LogStorageFailedException;

  Iterable<? extends DataSetLogEntry> getLogsFrom(long currentVersion);
}
