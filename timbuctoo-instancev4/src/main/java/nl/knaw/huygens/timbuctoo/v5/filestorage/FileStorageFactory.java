package nl.knaw.huygens.timbuctoo.v5.filestorage;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.IOException;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface FileStorageFactory {
  FileStorage makeFileStorage(String userId, String dataSetId) throws IOException;

  LogStorage makeLogStorage(String userId, String dataSetId) throws IOException;
}
