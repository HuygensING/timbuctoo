package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.google.common.base.Stopwatch;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class BerkeleyStore implements RdfProcessor, AutoCloseable {

  protected final BdbWrapper<String, String> bdbWrapper;
  private Stopwatch stopwatch;
  private static final Logger LOG = getLogger(BerkeleyStore.class);
  private int currentVersion = -1;

  protected BerkeleyStore(BdbDatabaseCreator dbEnvironment, String databaseName, String userId, String datasetId)
    throws DataStoreCreationException {
    this.bdbWrapper = dbEnvironment.getDatabase(
      userId,
      datasetId,
      databaseName,
      getDatabaseConfig(),
      TupleBinding.getPrimitiveBinding(String.class),
      TupleBinding.getPrimitiveBinding(String.class)
    );
  }

  protected abstract DatabaseConfig getDatabaseConfig();

  @Override
  public void start(int index) throws RdfProcessingFailedException {
    currentVersion = index;
    bdbWrapper.beginTransaction();
    stopwatch = Stopwatch.createStarted();
  }

  @Override
  public int getCurrentVersion() {
    return currentVersion;
  }

  @Override
  public void commit() throws RdfProcessingFailedException {
    LOG.info("processing took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds (pre-sync)");
    stopwatch.reset();
    stopwatch.start();
    bdbWrapper.commit();
    LOG.info("Sync took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
  }

  @Override
  public void close() throws DatabaseException {
    bdbWrapper.close();
  }

}
