package nl.knaw.huygens.timbuctoo.datastorage;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.datastorage.exceptions.DataStorageSaveException;
import nl.knaw.huygens.timbuctoo.filestorage.ChangeLogStorage;
import nl.knaw.huygens.timbuctoo.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.filestorage.LogStorage;
import nl.knaw.huygens.timbuctoo.jsonfilebackeddata.JsonDataStore;
import nl.knaw.huygens.timbuctoo.rdfio.RdfIoFactory;

import java.io.File;
import java.io.IOException;

public interface DataSetStorage {
  void saveMetaData(DataSetMetaData metaData) throws DataStorageSaveException;

  FileStorage getFileStorage() throws IOException;

  LogStorage getLogStorage() throws IOException;

  ChangeLogStorage getChangeLogStorage();

  RdfIoFactory getRdfIo();

  File getResourceSyncDescriptionFile();

  File getCustomSchemaFile();

  JsonDataStore<LogList> getLogList() throws IOException;

  void clear() throws IOException;

  File getCustomProvenanceFile();
}
