package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseConfig;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;

import java.util.stream.Stream;

public class VersionStore {

  private final BdbWrapper<String, Integer> bdbWrapper;

  public VersionStore(BdbDatabaseCreator dbFactory, String userId, String datasetId)
    throws DataStoreCreationException {

    DatabaseConfig rdfConfig = new DatabaseConfig();
    rdfConfig.setAllowCreate(true);
    rdfConfig.setDeferredWrite(true);

    bdbWrapper = dbFactory.getDatabase(
      userId,
      datasetId,
      "versions",
      rdfConfig,
      TupleBinding.getPrimitiveBinding(String.class),
      TupleBinding.getPrimitiveBinding(Integer.class)
    );
  }

  public int getVersion(String datastore) {
    try (Stream<Integer> values = bdbWrapper.databaseGetter()
      .startAtKey(datastore)
      .getAllWithSameKey(true)
      .getValues()) {
      return values.findAny().orElse(-1);
    }
  }

  public void setVersion(String datastore, int version) throws DatabaseWriteException {
    bdbWrapper.put(datastore, version);
  }
}
