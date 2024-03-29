package nl.knaw.huygens.timbuctoo.datastorage;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface DataStorage {
  boolean dataSetExists(String ownerId, String dataSetName);

  DataSetStorage getDataSetStorage(String ownerId, String dataSetName);

  Map<String, Set<DataSetMetaData>> loadDataSetMetaData() throws IOException;
}
