package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores;

import com.google.common.base.Stopwatch;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import nl.knaw.huygens.timbuctoo.v5.bdb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.bdb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.bdb.DatabaseFunction;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class BerkeleyStore implements RdfProcessor, AutoCloseable {

  private final BdbWrapper bdbWrapper;
  private Stopwatch stopwatch;
  private Transaction transaction;
  private final DatabaseConfig databaseConfig;
  private final DatabaseEntry keyEntry = new DatabaseEntry();
  private final DatabaseEntry valueEntry = new DatabaseEntry();
  private static final Logger LOG = getLogger(BerkeleyStore.class);
  protected final EntryBinding<String> binder = TupleBinding.getPrimitiveBinding(String.class);


  protected BerkeleyStore(BdbDatabaseCreator dbEnvironment, String databaseName, String userId, String datasetId)
    throws DataStoreCreationException {
    databaseConfig = getDatabaseConfig();
    this.bdbWrapper = dbEnvironment.getDatabase(userId, datasetId, databaseName, databaseConfig);
  }

  protected abstract DatabaseConfig getDatabaseConfig();

  @Override
  public void start() throws RdfProcessingFailedException {
    transaction = bdbWrapper.beginTransaction();
    stopwatch = Stopwatch.createStarted();
    LOG.info("Started importing...");
  }

  @Override
  public void finish() throws RdfProcessingFailedException {
    LOG.info("Finished importing. It took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds (pre-sync)");
    stopwatch.reset();
    stopwatch.start();
    bdbWrapper.commit(transaction);
    bdbWrapper.sync();
    LOG.info("Sync took " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
  }

  public <T> Stream<T> getItems(DatabaseFunction initialLookup,
                                DatabaseFunction iteration,
                                Supplier<T> valueMaker) {
    return bdbWrapper.getItems(initialLookup, iteration, valueMaker);
  }

  @Override
  public void close() throws DatabaseException {
    bdbWrapper.close(transaction);
  }

  public List<String> dump(String prefix, int start, int count, LockMode lockMode) throws DatabaseException {
    return bdbWrapper.dump(prefix, start, count, lockMode);
  }

  protected void put(String key, String value) throws DatabaseException {
    synchronized (keyEntry) {
      binder.objectToEntry(key, keyEntry);
      binder.objectToEntry(value, valueEntry);
      bdbWrapper.put(transaction, keyEntry, valueEntry);
    }
  }


}
