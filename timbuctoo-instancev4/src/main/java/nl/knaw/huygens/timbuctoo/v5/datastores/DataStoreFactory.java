package nl.knaw.huygens.timbuctoo.v5.datastores;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface DataStoreFactory {
  DataStores getDataStores(String userId, String datasetId) throws DataStoreCreationException;

  void start() throws Exception;

  void stop() throws Exception;
}
