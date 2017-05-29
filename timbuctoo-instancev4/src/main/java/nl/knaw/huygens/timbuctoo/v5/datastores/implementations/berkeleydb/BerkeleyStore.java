package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import com.google.common.base.Charsets;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.BdbDatabaseFactory;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

public abstract class BerkeleyStore implements RdfProcessor, AutoCloseable {

  protected final Environment dbEnvironment;
  protected final Database database;
  private Transaction transaction;
  private Set<Cursor> cursors;
  private final DatabaseConfig databaseConfig;
  private final DatabaseEntry keyEntry = new DatabaseEntry();
  private final DatabaseEntry valueEntry = new DatabaseEntry();

  protected BerkeleyStore(BdbDatabaseFactory dbEnvironment, String databaseName, String userId, String datasetId)
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
    for (Cursor cursor : cursors) {
      cursor.close();
    }
    database.close();
  }

  protected void put(String key, String value) throws DatabaseException {
    keyEntry.setData(key.getBytes(Charsets.UTF_8));
    valueEntry.setData(value.getBytes(Charsets.UTF_8));
    database.put(transaction, keyEntry, valueEntry);
  }

  public interface DatabaseFunction {
    OperationStatus apply(Cursor cursor) throws DatabaseException;
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
