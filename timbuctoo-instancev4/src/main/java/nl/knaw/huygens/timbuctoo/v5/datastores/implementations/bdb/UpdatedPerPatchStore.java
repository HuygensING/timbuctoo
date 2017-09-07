package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseConfig;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;

import java.util.stream.Stream;

public class UpdatedPerPatchStore {

  private final BdbWrapper<Integer, String> bdbWrapper;

  public UpdatedPerPatchStore(BdbDatabaseCreator dbFactory, String userId, String datasetId)
    throws DataStoreCreationException {

    DatabaseConfig config = new DatabaseConfig();
    config.setSortedDuplicates(true);
    config.setAllowCreate(true);
    config.setDeferredWrite(true);

    this.bdbWrapper = dbFactory.getDatabase(
      userId,
      datasetId,
      "updatedPerPatch",
      config,
      TupleBinding.getPrimitiveBinding(Integer.class),
      TupleBinding.getPrimitiveBinding(String.class)
    );
  }

  public void put(int currentversion, String subject) throws DatabaseWriteException {
    bdbWrapper.put(currentversion, subject);
  }

  public Stream<String> ofVersion(int version) {
    return bdbWrapper.databaseGetter().key(version).dontSkip().forwards().getValues();
  }
}
