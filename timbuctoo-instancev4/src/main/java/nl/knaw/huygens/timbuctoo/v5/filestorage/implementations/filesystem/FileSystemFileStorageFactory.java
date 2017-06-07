package nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorageFactory;
import nl.knaw.huygens.timbuctoo.v5.filestorage.LogStorage;

import java.io.File;
import java.io.IOException;

public class FileSystemFileStorageFactory implements FileStorageFactory {
  final File rootDir;

  @JsonCreator
  public FileSystemFileStorageFactory(@JsonProperty("rootDir") String rootDir) {
    this.rootDir = new File(rootDir);
  }

  public String getRootDir() {
    return rootDir.getAbsolutePath();
  }

  @Override
  public FileStorage makeFileStorage(String userId, String dataSetId) throws IOException {
    return new FileSystemFileStorage(createPath(userId, dataSetId));
  }

  @Override
  public LogStorage makeLogStorage(String userId, String dataSetId) throws IOException {
    return new FileSystemFileStorage(createPath(userId, dataSetId));
  }

  private File createPath(String userId, String dataSetId) {
    return new File(new File(new File(rootDir, userId), dataSetId), "files");
  }
}
