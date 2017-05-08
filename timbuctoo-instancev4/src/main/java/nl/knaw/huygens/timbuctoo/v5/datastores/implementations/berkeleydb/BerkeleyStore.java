package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatusImpl;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class BerkeleyStore implements AutoCloseable {

  protected final Environment dbEnvironment;
  protected final Database database;
  private Transaction transaction;
  private Set<Cursor> cursors = new HashSet<>();
  private final DatabaseConfig databaseConfig;
  private final DatabaseEntry keyEntry = new DatabaseEntry();
  private final DatabaseEntry valueEntry = new DatabaseEntry();
  protected final TupleBinding<String> binding;
  protected final StoreStatusImpl storeStatus;
  private final ObjectMapper objectMapper;
  private static final Logger LOG = getLogger(BerkeleyStore.class);

  protected BerkeleyStore(Environment dbEnvironment, String databaseName, ObjectMapper objectMapper)
      throws DatabaseException {
    this.objectMapper = objectMapper;
    this.dbEnvironment = dbEnvironment;
    databaseConfig = getDatabaseConfig();
    database = dbEnvironment.openDatabase(null, databaseName, databaseConfig);
    binding = TupleBinding.getPrimitiveBinding(String.class);
    Optional<String> statusRecord = getItem("\nstatus");
    if (statusRecord.isPresent()) {
      StoreStatusImpl storeStatus;
      try {
        storeStatus = this.objectMapper.readValue(statusRecord.get(), StoreStatusImpl.class);
      } catch (IOException e) {
        storeStatus = new StoreStatusImpl(0);
      }
      this.storeStatus = storeStatus;
    } else {
      storeStatus = new StoreStatusImpl(0);
    }
  }

  protected abstract DatabaseConfig getDatabaseConfig();

  public void startTransaction() throws LogProcessingFailedException {
    if (databaseConfig.getTransactional()) {
      try {
        transaction = dbEnvironment.beginTransaction(null, null);
      } catch (DatabaseException e) {
        throw new LogProcessingFailedException(e);
      }
    }
  }

  public void commitTransaction() throws LogProcessingFailedException {
    if (databaseConfig.getTransactional()) {
      try {
        transaction.commit();
        transaction = null;
      } catch (DatabaseException e) {
        throw new LogProcessingFailedException(e);
      }
    }
  }


  public List<String> dump(String prefix, int start, int count, LockMode lockMode) throws DatabaseException {
    DatabaseEntry key = new DatabaseEntry();
    binding.objectToEntry(prefix, key);
    DatabaseEntry value = new DatabaseEntry();

    Cursor cursor = database.openCursor(null, null);
    OperationStatus status = cursor.getSearchKeyRange(key, value, LockMode.READ_UNCOMMITTED);
    List<String> result = new ArrayList<>();
    int index = 0;
    while (status == OperationStatus.SUCCESS && index < (start + count)) {
      if (index >= start) {
        result.add(
          binding.entryToObject(key) + " -> " + binding.entryToObject(value)
        );
      }
      index++;
      status = cursor.getNext(key, value, LockMode.READ_UNCOMMITTED);
    }
    cursor.close();
    return result;
  }


  @Override
  public void close() throws Exception {
    for (Cursor cursor : cursors) {
      cursor.close();
    }
    try {
      startTransaction();
      put("\nstatus", objectMapper.writeValueAsString(storeStatus));
      commitTransaction();
    } catch (DatabaseException e) {
      LOG.error("Writing out status failed", e);
    }
    database.close();
  }


  protected void put(String key, String value) throws DatabaseException {
    binding.objectToEntry(key, keyEntry);
    binding.objectToEntry(value, valueEntry);
    database.put(transaction, keyEntry, valueEntry);
  }

  public interface DatabaseFunction {
    OperationStatus apply(Cursor cursor) throws DatabaseException;
  }

  public Optional<String> getItem(String key) throws DatabaseException {
    DatabaseEntry keyEntry = new DatabaseEntry();
    DatabaseEntry value = new DatabaseEntry();

    binding.objectToEntry(key, keyEntry);

    Cursor cursor = database.openCursor(null, null);
    final OperationStatus status = cursor.getSearchKey(keyEntry, value, LockMode.DEFAULT);
    if (status == OperationStatus.SUCCESS) {
      String result = binding.entryToObject(value);
      cursor.close();
      return Optional.of(result);
    } else {
      cursor.close();
      return Optional.empty();
    }
  }

  public <T> AutoCloseableIterator<T> getItems(DatabaseFunction initialLookup, DatabaseFunction iteration,
                                               Supplier<T> valueMaker) {
    return new AutoCloseableIterator<T>() {

      @Override
      public void close() {
        try {
          if (cursor != null) {
            cursor.close();
          }
        } catch (DatabaseException e) {
          //FIXME! log error
          e.printStackTrace();
        }
      }

      Cursor cursor = null;
      boolean shouldMove = true;
      OperationStatus status = null;

      @Override
      public boolean hasNext() {
        if (shouldMove) {
          try {
            if (cursor == null) {
              cursor = database.openCursor(null, null);
              status = initialLookup.apply(cursor);
            } else {
              status = iteration.apply(cursor);
            }
          } catch (DatabaseException e) {
            //FIXME! log error
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
    };
  }

}
