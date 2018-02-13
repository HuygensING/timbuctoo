package nl.knaw.huygens.timbuctoo.v5.datastorage;

import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface DataStorage {
  DataSetStorage getDataSetStorage(String ownerId, String dataSetName);

  Map<String, Set<DataSetMetaData>> loadDataSetMetaData() throws IOException;

  void deleteDataSetData(String ownerId, String dataSetName, int retryCount);
}
