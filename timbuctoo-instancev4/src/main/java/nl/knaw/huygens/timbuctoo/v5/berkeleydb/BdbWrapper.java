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

  public DatabaseGetter.DatabaseGetterBuilder<KeyT, ValueT> databaseGetter() {
    return DatabaseGetter.databaseGetter(keyBinder, valueBinder, database, cursors);
  }

  public void commit() {
    if (transaction != null) {
      transaction.commit();
    }
    database.sync();
  }

  public void put(KeyT key, ValueT value) throws DatabaseWriteException {
    synchronized (keyEntry) {
      try {
        keyBinder.objectToEntry(key, keyEntry);
        valueBinder.objectToEntry(value, valueEntry);
        database.put(transaction, keyEntry, valueEntry);
      } catch (Exception e) {
        throw new DatabaseWriteException(e);
      }
    }
  }

  public void delete(KeyT key, ValueT value) throws DatabaseWriteException {
    Cursor cursor = database.openCursor(transaction, CursorConfig.DEFAULT);
    synchronized (keyEntry) {
      try {
        keyBinder.objectToEntry(key, keyEntry);
        valueBinder.objectToEntry(value, valueEntry);
        OperationStatus searchBoth = cursor.getSearchBoth(keyEntry, valueEntry, LockMode.DEFAULT);
        if (searchBoth.equals(OperationStatus.SUCCESS)) {
          cursor.delete();
        }
      } catch (Exception e) {
        throw new DatabaseWriteException(e);
      }
    }
    cursor.close();
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
