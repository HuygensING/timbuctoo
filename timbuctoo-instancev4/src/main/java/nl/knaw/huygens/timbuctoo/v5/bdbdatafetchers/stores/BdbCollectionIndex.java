package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import nl.knaw.huygens.timbuctoo.v5.bdb.DatabaseFunction;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorSubject;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.bdb.BdbDatabaseCreator;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class BdbCollectionIndex extends BerkeleyStore implements RdfProcessor, AutoCloseable, CollectionIndex {
  public BdbCollectionIndex(DataProvider dataProvider, BdbDatabaseCreator factory, String userId, String dataSetId)
    throws DataStoreCreationException {
    super(factory, "collectionIndex", userId, dataSetId);
    dataProvider.subscribeToRdf(this, null);
  }

  @Override
  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig rdfConfig = new DatabaseConfig();
    rdfConfig.setAllowCreate(true);
    rdfConfig.setSortedDuplicates(true);
    rdfConfig.setDeferredWrite(true);
    return rdfConfig;
  }

  @Override
  public void setPrefix(String cursor, String prefix, String iri) {}

  @Override
  public void addRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
    if (predicate.equals(RDF_TYPE)) {
      try {
        put(object, subject);
      } catch (DatabaseException e) {
        throw new RdfProcessingFailedException(e);
      }
    }
  }

  @Override
  public void delRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
    //FIXME: implement
  }


  @Override
  public void addValue(String cursor, String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {}

  @Override
  public void addLanguageTaggedString(String cursor, String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {}

  @Override
  public void delValue(String cursor, String subject, String predicate, String value, String valueType, String graph)
      throws RdfProcessingFailedException {}

  @Override
  public void delLanguageTaggedString(String cursor, String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {}

  @Override
  public Stream<CursorSubject> getSubjects(String collectionName, String cursor) {
    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry value = new DatabaseEntry();

    DatabaseFunction initializer;
    DatabaseFunction iterator;

    DatabaseFunction forwardMovingIterator = dbCursor -> dbCursor.getNextDup(key, value, LockMode.DEFAULT);
    DatabaseFunction backwardMovingIterator = dbCursor -> dbCursor.getPrevDup(key, value, LockMode.DEFAULT);

    if (cursor.isEmpty()) {
      //initializer starts at the collection
      binder.objectToEntry(collectionName, key);
      initializer = dbCursor -> dbCursor.getSearchKey(key, value, LockMode.DEFAULT);

      iterator = forwardMovingIterator;
    } else {
      if ("LAST".equals(cursor)) {
        //initializer starts at the next collection and moves one step back
        binder.objectToEntry(collectionName, key);
        initializer = dbCursor -> {
          OperationStatus status = dbCursor.getSearchKey(key, value, LockMode.DEFAULT);
          if (status == OperationStatus.SUCCESS) {
            status = dbCursor.getNextNoDup(key, value, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
              status = dbCursor.getPrev(key, value, LockMode.DEFAULT);
            } else {
              //go to end
              status = dbCursor.getLast(key, value, LockMode.DEFAULT);
            }
          }
          return status;
        };

        iterator = backwardMovingIterator;
      } else {
        //initializer starts at the cursor.substring(2)
        binder.objectToEntry(collectionName, key);
        binder.objectToEntry(cursor.substring(2), value);
        if (cursor.startsWith("D\n")) {
          iterator = backwardMovingIterator;
        } else {
          iterator = forwardMovingIterator;
        }
        initializer = dbCursor -> {
          OperationStatus status = dbCursor.getSearchBoth(key, value, LockMode.DEFAULT);
          if (status == OperationStatus.SUCCESS) {
            return iterator.apply(dbCursor);
          } else {
            return status;
          }
        };
      }
    }

    return getItems(
      initializer,
      iterator,
      () -> {
        String subject = binder.entryToObject(value);
        return CursorSubject.create(subject, subject);
      }
    );
  }


}
