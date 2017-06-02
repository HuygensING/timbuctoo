package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.BdbDatabaseCreator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class BerkeleyStore implements RdfProcessor, AutoCloseable {

  protected final Environment dbEnvironment;
  protected final Database database;
  private Transaction transaction;
  private final Map<Cursor, String> cursors = new HashMap<>();
  private final DatabaseConfig databaseConfig;
  private final DatabaseEntry keyEntry = new DatabaseEntry();
  private final DatabaseEntry valueEntry = new DatabaseEntry();
  private static final Logger LOG = getLogger(BerkeleyStore.class);
  protected final EntryBinding<String> binder = TupleBinding.getPrimitiveBinding(String.class);


  protected BerkeleyStore(BdbDatabaseCreator dbEnvironment, String databaseName, String userId, String datasetId)
    throws DataStoreCreationException {
    databaseConfig = getDatabaseConfig();
    Tuple<Environment, Database> database = dbEnvironment.getDatabase(userId, datasetId, databaseName, databaseConfig);
    this.dbEnvironment = database.getLeft();
    this.database = database.getRight();
  }

  protected abstract DatabaseConfig getDatabaseConfig();

  @Override
  public void start() throws RdfProcessingFailedException {
    if (databaseConfig.getTransactional()) {
      try {
        transaction = dbEnvironment.beginTransaction(null, null);
      } catch (DatabaseException e) {
        throw new RdfProcessingFailedException(e);
      }
    }
  }

  @Override
  public void finish() throws RdfProcessingFailedException {
    if (databaseConfig.getTransactional()) {
      try {
        transaction.commit();
        transaction = null;
      } catch (DatabaseException e) {
        throw new RdfProcessingFailedException(e);
      }
    }
  }

  @Override
  public void close() throws DatabaseException {
    if (databaseConfig.getTransactional()) {
      if (transaction != null) {
        transaction.abort();
      }
    }
    for (Map.Entry<Cursor, String> cursor : cursors.entrySet()) {
      cursor.getKey().close();
      LOG.error("Cursor was not closed. It was opened at: \n" + cursor.getValue());
    }
    database.close();
  }

  public List<String> dump(String prefix, int start, int count, LockMode lockMode) throws DatabaseException {
    DatabaseEntry key = new DatabaseEntry();
    binder.objectToEntry(prefix, key);
    DatabaseEntry value = new DatabaseEntry();

    Cursor cursor = database.openCursor(null, null);
    OperationStatus status = cursor.getSearchKeyRange(key, value, LockMode.READ_UNCOMMITTED);
    List<String> result = new ArrayList<>();
    int index = 0;
    while (status == OperationStatus.SUCCESS && index < (start + count)) {
      if (index >= start) {
        result.add(
          binder.entryToObject(key) + " -> " + binder.entryToObject(value)
        );
      }
      index++;
      status = cursor.getNext(key, value, LockMode.READ_UNCOMMITTED);
    }
    cursor.close();
    return result;
  }

  protected void put(String key, String value) throws DatabaseException {
    synchronized (keyEntry) {
      binder.objectToEntry(key, keyEntry);
      binder.objectToEntry(value, valueEntry);
      database.put(transaction, keyEntry, valueEntry);
    }
  }

  public interface DatabaseFunction {
    OperationStatus apply(Cursor cursor) throws DatabaseException;
  }

  public <T> Stream<T> getItems(DatabaseFunction initialLookup, DatabaseFunction iteration,
                                Supplier<T> valueMaker) {
    CursorIterator<T> data = new CursorIterator<>(
      initialLookup,
      iteration,
      valueMaker,
      ExceptionUtils.getStackTrace(new Throwable())
    );

    return stream(data).onClose(() -> {
      try {
        if (data.cursor != null) {
          data.cursor.close();
          cursors.remove(data.cursor);
        }
      } catch (DatabaseException e) {
        LOG.error("Could not close cursor", e);
      }
    });
  }

  private class CursorIterator<T> implements Iterator<T> {
    private final DatabaseFunction initialLookup;
    private final DatabaseFunction iteration;
    private final Supplier<T> valueMaker;
    private final String stackTrace;
    public Cursor cursor;
    boolean shouldMove;
    OperationStatus status;

    public CursorIterator(DatabaseFunction initialLookup, DatabaseFunction iteration,
                          Supplier<T> valueMaker, String stackTrace) {
      this.initialLookup = initialLookup;
      this.iteration = iteration;
      this.valueMaker = valueMaker;
      this.stackTrace = stackTrace;
      cursor = null;
      shouldMove = true;
      status = null;
    }

    @Override
    public boolean hasNext() {
      if (shouldMove) {
        try {
          if (cursor == null) {
            cursor = database.openCursor(null, null);
            cursors.put(cursor, stackTrace);
            status = initialLookup.apply(cursor);
          } else {
            status = iteration.apply(cursor);
          }
        } catch (DatabaseException e) {
          LOG.error("Database exception!", e);
          status = OperationStatus.NOTFOUND;
        }
        shouldMove = false;
      }
      return status == OperationStatus.SUCCESS;
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      shouldMove = true;
      return valueMaker.get();
    }
  }
}
