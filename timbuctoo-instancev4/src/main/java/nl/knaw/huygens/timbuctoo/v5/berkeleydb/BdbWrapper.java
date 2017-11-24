package nl.knaw.huygens.timbuctoo.v5.berkeleydb;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.sleepycat.je.OperationStatus.SUCCESS;
import static org.slf4j.LoggerFactory.getLogger;

public class BdbWrapper<KeyT, ValueT> {
  private final Environment dbEnvironment;
  private final Database database;
  private final DatabaseConfig databaseConfig;
  private final EntryBinding<KeyT> keyBinder;
  private final EntryBinding<ValueT> valueBinder;
  private final DatabaseEntry keyEntry = new DatabaseEntry();
  private final DatabaseEntry valueEntry = new DatabaseEntry();
  private Transaction transaction;
  private static final Logger LOG = getLogger(BdbWrapper.class);
  private final Map<Cursor, String> cursors = new HashMap<>();

  public BdbWrapper(Environment dbEnvironment, Database database, DatabaseConfig databaseConfig,
                    EntryBinding<KeyT> keyBinder, EntryBinding<ValueT> valueBinder) {
    this.dbEnvironment = dbEnvironment;
    this.database = database;
    this.databaseConfig = databaseConfig;
    this.keyBinder = keyBinder;
    this.valueBinder = valueBinder;
  }

  public void beginTransaction() {
    if (databaseConfig.getTransactional()) {
      transaction = dbEnvironment.beginTransaction(null, null);
    }
  }

  public void close() {
    if (transaction != null) {
      transaction.abort();
    }
    for (Map.Entry<Cursor, String> cursor : cursors.entrySet()) {
      cursor.getKey().close();
      LOG.error("Cursor was not closed. It was opened at: \n" + cursor.getValue());
    }

    database.close();
  }

  public DatabaseGetter.Builder<KeyT, ValueT> databaseGetter() {
    return DatabaseGetter.databaseGetter(keyBinder, valueBinder, database, cursors);
  }

  public void commit() {
    if (transaction != null) {
      transaction.commit();
    }
    database.sync();
  }

  public void replace(KeyT key, ValueT initialValue, Function<ValueT, ValueT> replacer) throws DatabaseWriteException {
    synchronized (keyEntry) {
      try (Cursor cursor = database.openCursor(transaction, CursorConfig.DEFAULT)) {
        keyBinder.objectToEntry(key, keyEntry);
        OperationStatus searchResult = cursor.getSearchKey(keyEntry, valueEntry, LockMode.DEFAULT);
        ValueT newValue = initialValue;
        if (searchResult.equals(OperationStatus.SUCCESS)) {
          newValue = replacer.apply(valueBinder.entryToObject(valueEntry));
        }
        valueBinder.objectToEntry(newValue, valueEntry);
        cursor.putCurrent(valueEntry);
      } catch (Exception e) {
        throw new DatabaseWriteException(e);
      }
    }
  }

  public boolean put(KeyT key, ValueT value) throws DatabaseWriteException {
    synchronized (keyEntry) {
      try {
        keyBinder.objectToEntry(key, keyEntry);
        if (databaseConfig.getSortedDuplicates()) {
          valueBinder.objectToEntry(value, valueEntry);
          return database.putNoDupData(transaction, keyEntry, valueEntry) != null;
        } else {
          try (Cursor cursor = database.openCursor(transaction, CursorConfig.DEFAULT)) {
            OperationStatus searchResult = cursor.getSearchKey(keyEntry, valueEntry, LockMode.DEFAULT);
            if (searchResult == SUCCESS && Objects.equals(value, valueBinder.entryToObject(valueEntry))) {
              return false;
            } else {
              valueBinder.objectToEntry(value, valueEntry);
              return database.put(transaction, keyEntry, valueEntry) != null;
            }
          }
        }
      } catch (Exception e) {
        throw new DatabaseWriteException(e);
      }
    }
  }

  public boolean delete(KeyT key, ValueT value) throws DatabaseWriteException {
    boolean wasChange = false;
    synchronized (keyEntry) {
      try (Cursor cursor = database.openCursor(transaction, CursorConfig.DEFAULT)) {
        keyBinder.objectToEntry(key, keyEntry);
        valueBinder.objectToEntry(value, valueEntry);
        OperationStatus searchResult = cursor.getSearchBoth(keyEntry, valueEntry, LockMode.DEFAULT);
        if (searchResult.equals(OperationStatus.SUCCESS)) {
          wasChange = cursor.delete() == OperationStatus.SUCCESS;
        }
      } catch (Exception e) {
        throw new DatabaseWriteException(e);
      }
    }
    return wasChange;
  }

  public List<String> dump(String prefix, int start, int count, LockMode lockMode) {
    EntryBinding<String> binder = TupleBinding.getPrimitiveBinding(String.class);
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
}
