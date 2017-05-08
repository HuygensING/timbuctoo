package nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore;

import nl.knaw.huygens.timbuctoo.v5.logprocessing.DataSetLogEntry;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.LocalData;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.RdfCreator;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogStorageFailedException;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

public interface LogStorage {
  LocalData getLog(URI logUri) throws LogStorageFailedException;

  LocalData saveLog(URI identifier, Optional<String> mimeType, Optional<Charset> charset, InputStream rdfInputStream)
    throws LogStorageFailedException;

  LocalData startOrContinueAppendLog(RdfCreator generator) throws LogStorageFailedException;

  Iterable<? extends DataSetLogEntry> getLogsFrom(long currentVersion);
}
