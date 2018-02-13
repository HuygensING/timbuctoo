package nl.knaw.huygens.timbuctoo.v5.datastorage.implementations.filesystem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetConfiguration;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.LogList;
import nl.knaw.huygens.timbuctoo.v5.datastorage.DataSetStorage;
import nl.knaw.huygens.timbuctoo.v5.datastorage.exceptions.DataStorageSaveException;
import nl.knaw.huygens.timbuctoo.v5.filehelper.FileHelper;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.v5.filestorage.LogStorage;
import nl.knaw.huygens.timbuctoo.v5.jacksonserializers.TimbuctooCustomSerializers;
import nl.knaw.huygens.timbuctoo.v5.jsonfilebackeddata.JsonDataStore;
import nl.knaw.huygens.timbuctoo.v5.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfIoFactory;

import java.io.File;
import java.io.IOException;

class FileSystemDataSetStorage implements DataSetStorage {

  private final String ownerPrefix;
  private final String dataSetId;
  private final FileHelper fileHelper;
  private final DataSetConfiguration configuration;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .registerModule(new Jdk8Module())
    .registerModule(new GuavaModule())
    .registerModule(new TimbuctooCustomSerializers())
    .enable(SerializationFeature.INDENT_OUTPUT);
  private final File logFile;

  FileSystemDataSetStorage(String ownerId, String dataSetId, FileHelper fileHelper,
                           DataSetConfiguration configuration) {
    this.ownerPrefix = ownerId;
    this.dataSetId = dataSetId;
    this.fileHelper = fileHelper;
    this.configuration = configuration;
    logFile = fileHelper.fileInDataSet(ownerPrefix, dataSetId, "log.json");
  }

  @Override
  public void saveMetaData(DataSetMetaData metaData) throws DataStorageSaveException {
    File metaDataFile = fileHelper.fileInDataSet(ownerPrefix, dataSetId, "metaData.json");

    try {
      OBJECT_MAPPER.writeValue(metaDataFile, metaData);
    } catch (IOException e) {
      throw new DataStorageSaveException(e);
    }
  }

  @Override
  public FileStorage getFileStorage() throws IOException {
    return configuration.getFileStorage().makeFileStorage(ownerPrefix, dataSetId);
  }

  @Override
  public LogStorage getLogStorage() throws IOException {
    return configuration.getFileStorage().makeLogStorage(ownerPrefix, dataSetId);
  }

  @Override
  public RdfIoFactory getRdfIo() {
    return configuration.getRdfIo();
  }

  @Override
  public File getResourceSyncDescriptionFile() {
    return fileHelper.fileInDataSet(ownerPrefix, dataSetId, "description.xml");
  }

  @Override
  public JsonDataStore<LogList> getLogList() throws IOException {
    return JsonFileBackedData.getOrCreate(logFile, LogList::new, new TypeReference<LogList>(){});
  }
}
