package nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceList;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.filestorage.LogStorage;

import java.io.File;
import java.io.IOException;

public class FileSystemFileStorageFactory implements FileStorageFactory {
  private final DataSetPathHelper dataSetPathHelper;

  @JsonCreator
  public FileSystemFileStorageFactory(@JsonProperty("rootDir") String rootDir) {
    this.dataSetPathHelper = new DataSetPathHelper(new File(rootDir));
  }

  @Override
  public FileStorage makeFileStorage(String userId, String dataSetId)
    throws IOException {
    File filePath = dataSetPathHelper.pathInDataSet(userId, dataSetId, "files");
    return new FileSystemFileStorage(filePath);
  }

  @Override
  public LogStorage makeLogStorage(String userId, String dataSetId)
    throws IOException {
    File filePath = dataSetPathHelper.pathInDataSet(userId, dataSetId, "files");
    return new FileSystemFileStorage(filePath);
  }

}
