package nl.knaw.huygens.timbuctoo.v5.dropwizard;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;

public interface BdbDatabaseCreator {
  Tuple<Environment, Database> getDatabase(String userId, String dataSetId, String databaseName,
                                           DatabaseConfig config)
    throws DataStoreCreationException;
}
