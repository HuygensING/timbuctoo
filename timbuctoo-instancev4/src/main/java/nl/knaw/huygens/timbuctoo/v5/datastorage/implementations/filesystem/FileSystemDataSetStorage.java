package nl.knaw.huygens.timbuctoo.v5.datastorage.implementations.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.datastorage.DataSetStorage;
import nl.knaw.huygens.timbuctoo.v5.datastorage.exceptions.DataStorageSaveException;
import nl.knaw.huygens.timbuctoo.v5.filehelper.FileHelper;
import nl.knaw.huygens.timbuctoo.v5.jacksonserializers.TimbuctooCustomSerializers;

import java.io.File;
import java.io.IOException;

class FileSystemDataSetStorage implements DataSetStorage {

  private final String ownerPrefix;
  private final String dataSetId;
  private final FileHelper fileHelper;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .registerModule(new Jdk8Module())
    .registerModule(new GuavaModule())
    .registerModule(new TimbuctooCustomSerializers())
    .enable(SerializationFeature.INDENT_OUTPUT);

  FileSystemDataSetStorage(String ownerId, String dataSetId, FileHelper fileHelper) {
    this.ownerPrefix = ownerId;
    this.dataSetId = dataSetId;
    this.fileHelper = fileHelper;
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
}
