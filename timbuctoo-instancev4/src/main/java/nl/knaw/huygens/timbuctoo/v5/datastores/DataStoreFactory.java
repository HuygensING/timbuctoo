package nl.knaw.huygens.timbuctoo.v5.datastores;

import com.sleepycat.je.DatabaseException;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.DataStores;

public interface DataStoreFactory {
  DataStores getDataStores(String dataSetName) throws DatabaseException;
}
