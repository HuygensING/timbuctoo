package nl.knaw.huygens.timbuctoo.v5.berkeleydb;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static org.slf4j.LoggerFactory.getLogger;

public class DatabaseGetter<T> {
  private final DatabaseEntry key;
  private final DatabaseEntry value;
  private final EntryBinding<T> binder;
  private final DatabaseFunction initializer;
  private final DatabaseFunction iterator;
  private final Database database;
  private final Map<Cursor, String> cursors;

  private static final Logger LOG = getLogger(DatabaseGetter.class);

  DatabaseGetter(EntryBinding<T> binder, DatabaseFunction initializer, DatabaseFunction iterator, Database database,
                 Map<Cursor, String> cursors, DatabaseEntry key, DatabaseEntry value) {
    this.binder = binder;
    this.initializer = initializer;
    this.iterator = iterator;
    this.database = database;
    this.cursors = cursors;
    this.key = key;
    this.value = value;
  }


  public Stream<T> getKeys() {
    return getItems(() -> binder.entryToObject(key));
  }

  public Stream<T> getValues() {
    return getItems(() -> binder.entryToObject(value));

  }

  public <U> Stream<U> getKeysAndValues(BiFunction<T, T, U> valueMaker) {
    return getItems(() -> valueMaker.apply(binder.entryToObject(key), binder.entryToObject(value)));
  }

  private <U> Stream<U> getItems(Supplier<U> valueMaker) {
    CursorIterator<U> data = new CursorIterator<>(
      initializer,
      iterator,
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

  public static <T> DatabaseGetterBuilder<T> databaseGetter(EntryBinding<T> binder, Database database,
                                                            Map<Cursor, String> cursors) {
    return new DatabaseGetterBuilderImpl<>(binder, database, cursors);
  }

  private interface DatabaseFunctionMaker<T> {
    DatabaseFunction make(DatabaseEntry keyEntry, DatabaseEntry valueEntry, DatabaseFunction iterator,
                          EntryBinding<T> binder);
  }

  public interface DatabaseGetterBuilder<T> {
    DatabaseGetter<T> getAll();

    DatabaseGetterBuilderWithInitializer<T> startAtKey(T key);

    DatabaseGetterBuilderWithInitializer<T> startAtEndOfKeyDuplicates(T key);

    DatabaseGetterBuilderWithInitializer<T> startAfterValue(T key, T value);
  }

  public interface DatabaseGetterBuilderWithInitializer<T> {
    DatabaseGetter<T> getAllWithSameKey(boolean forwards);
  }

  public static class DatabaseGetterBuilderImpl<T> implements DatabaseGetterBuilder<T>,
    DatabaseGetterBuilderWithInitializer<T> {

    private final EntryBinding<T> binder;
    private final Database database;
    private final Map<Cursor, String> cursors;
    private DatabaseFunctionMaker<T> initializerMaker;

    public DatabaseGetterBuilderImpl(EntryBinding<T> binder, Database database, Map<Cursor, String> cursors) {
      this.binder = binder;
      this.database = database;
      this.cursors = cursors;
    }

    @Override
    public DatabaseGetter<T> getAll() {
      DatabaseEntry key = new DatabaseEntry();
      DatabaseEntry value = new DatabaseEntry();

      DatabaseFunction iterator = dbCursor -> dbCursor.getNext(key, value, LockMode.DEFAULT);

      return new DatabaseGetter<>(
        binder,
        iterator,
        iterator,
        database,
        cursors,
        key, value);
    }

    @Override
    public DatabaseGetterBuilderWithInitializer<T> startAtKey(T key) {
      initializerMaker = (keyEntry, valueEntry, iterator, binder) -> {
        binder.objectToEntry(key, keyEntry);
        return dbCursor -> dbCursor.getSearchKey(keyEntry, valueEntry, LockMode.DEFAULT);
      };
      return this;
    }

    @Override
    public DatabaseGetterBuilderWithInitializer<T> startAtEndOfKeyDuplicates(T key) {
      initializerMaker = (keyEntry, valueEntry, iterator, binder) -> {
        binder.objectToEntry(key, keyEntry);
        //go to next entry and move one step back
        return dbCursor -> {
          OperationStatus status = dbCursor.getSearchKey(keyEntry, valueEntry, LockMode.DEFAULT);
          if (status == OperationStatus.SUCCESS) {
            status = dbCursor.getNextNoDup(keyEntry, valueEntry, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
              status = dbCursor.getPrev(keyEntry, valueEntry, LockMode.DEFAULT);
            } else {
              //go to end
              status = dbCursor.getLast(keyEntry, valueEntry, LockMode.DEFAULT);
            }
          }
          return status;
        };
      };
      return this;
    }

    @Override
    public DatabaseGetterBuilderWithInitializer<T> startAfterValue(T key, T value) {
      initializerMaker = (keyEntry, valueEntry, iterator, binder) -> {
        binder.objectToEntry(key, keyEntry);
        binder.objectToEntry(value, valueEntry);
        return dbCursor -> {
          OperationStatus status = dbCursor.getSearchBoth(keyEntry, valueEntry, LockMode.DEFAULT);
          if (status == OperationStatus.SUCCESS) {
            return iterator.apply(dbCursor);
          } else {
            return status;
          }
        };
      };
      return this;
    }

    @Override
    public DatabaseGetter<T> getAllWithSameKey(boolean forwards) {
      DatabaseEntry key = new DatabaseEntry();
      DatabaseEntry value = new DatabaseEntry();
      DatabaseFunction iterator;
      if (forwards) {
        iterator = dbCursor -> dbCursor.getNextDup(key, value, LockMode.DEFAULT);
      } else {
        iterator = dbCursor -> dbCursor.getPrevDup(key, value, LockMode.DEFAULT);
      }
      return new DatabaseGetter<>(
        binder,
        initializerMaker.make(key, value, iterator, binder),
        iterator,
        database,
        cursors,
        key,
        value
      );
    }

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
