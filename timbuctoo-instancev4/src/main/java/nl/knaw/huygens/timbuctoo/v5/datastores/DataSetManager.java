package nl.knaw.huygens.timbuctoo.v5.datastores;

import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;

import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;

public interface DataSetManager {
  DataStores getDataStores(String dataSetId) throws IOException;

  void onDataSetsAvailable(Consumer<Set<String>> subscription);

  Set<String> getDataSets();

  void removeDataSet(String dataSetId) throws IOException;

  boolean exists(String dataSetId);
}
