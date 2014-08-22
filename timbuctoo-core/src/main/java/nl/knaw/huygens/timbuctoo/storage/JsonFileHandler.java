package nl.knaw.huygens.timbuctoo.storage;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A class that writes and reads json files.
 */
public class JsonFileHandler {

  public JsonFileHandler(Configuration configMock, ObjectMapper objectMapperMock) {
    // TODO Auto-generated constructor stub
  }

  public <T extends FileCollection<U>, U extends SystemEntity> void saveCollection(Class<T> type, T collection, String fileName) {
    // TODO Auto-generated method stub

  }

  public <T> T getCollection(Class<T> type, String fileName) {
    // TODO Auto-generated method stub
    return null;
  }

}
