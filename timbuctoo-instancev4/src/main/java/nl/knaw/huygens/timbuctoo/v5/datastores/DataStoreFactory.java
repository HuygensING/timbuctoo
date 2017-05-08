package nl.knaw.huygens.timbuctoo.v5.datastores;

import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;

import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;

public interface DataStoreFactory {
  DataStores getDataStores(String dataSetName) throws IOException;

  void onDataSetsAvailable(Consumer<Set<String>> subscription);

  Set<String> getDataSets();
}
