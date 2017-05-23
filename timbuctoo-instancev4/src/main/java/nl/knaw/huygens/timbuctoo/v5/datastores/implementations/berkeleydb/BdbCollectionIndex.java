package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.MarkedSubject;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatus;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatusImpl;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadLoader;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.ProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.ObjectMapperFactory;
import org.slf4j.Logger;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

public class BdbCollectionIndex extends BerkeleyStore implements CollectionIndex {
  private TripleWriter tripleWriter;
  private static final Logger LOG = getLogger(BdbCollectionIndex.class);


  public BdbCollectionIndex(String dataSetName, Environment dbEnvironment, ObjectMapperFactory objectMappers)
      throws DatabaseException {
    super(dbEnvironment, "collectionIndex_" + dataSetName, objectMappers);
  }

  @Override
  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig rdfConfig = new DatabaseConfig();
    rdfConfig.setAllowCreate(true);
    rdfConfig.setTransactional(true);
    rdfConfig.setSortedDuplicates(true);
    return rdfConfig;
  }

  @Override
  public Stream<MarkedSubject> getSubjects(String collectionName, boolean ascending) {
    DatabaseEntry key = new DatabaseEntry();
    binding.objectToEntry(collectionName, key);
    DatabaseEntry value = new DatabaseEntry();

    return getItems(
      cursor -> {
        if (ascending) {
          return cursor.getSearchKey(key, value, LockMode.DEFAULT);
        } else {
          OperationStatus status = cursor.getSearchKey(key, value, LockMode.DEFAULT);
          if (status == OperationStatus.SUCCESS) {
            status = cursor.getNextNoDup(key, value, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
              status = cursor.getPrev(key, value, LockMode.DEFAULT);
            } else {
              //go to end
              status = cursor.getLast(key, value, LockMode.DEFAULT);
            }
          }
          return status;
        }
      },
      cursor -> {
        if (ascending) {
          return cursor.getNextDup(key, value, LockMode.DEFAULT);
        } else {
          return cursor.getPrevDup(key, value, LockMode.DEFAULT);
        }
      },
      () -> {
        String parsedValue = binding.entryToObject(value);
        return MarkedSubject.create(parsedValue, parsedValue);
      }
    );
  }

  @Override
  public Stream<MarkedSubject> getSubjects(String collectionName, boolean ascending, String marker) {
    DatabaseEntry key = new DatabaseEntry();
    binding.objectToEntry(collectionName, key);
    DatabaseEntry value = new DatabaseEntry();
    binding.objectToEntry(marker, value);

    return getItems(
      cursor -> {
        OperationStatus result = cursor.getSearchBoth(key, value, LockMode.DEFAULT);
        if (result == OperationStatus.SUCCESS) {
          return cursor.getNextDup(key, value, LockMode.DEFAULT);
        }
        return result;
      },
      cursor -> cursor.getNextDup(key, value, LockMode.DEFAULT),
      () -> {
        String parsedValue = binding.entryToObject(value);
        return MarkedSubject.create(parsedValue, parsedValue);
      }
    );
  }

  @Override
  public void process(QuadLoader source, long version) throws ProcessingFailedException {
    try {
      tripleWriter = new TripleWriter(this, storeStatus, version);
      source.sendQuads(tripleWriter);
    } catch (LogProcessingFailedException e) {
      throw new ProcessingFailedException(e);
    }
  }

  @Override
  public StoreStatus getStatus() {
    return storeStatus;
  }

  private class TripleWriter extends TripleWriterBase {
    protected TripleWriter(BerkeleyStore store, StoreStatusImpl storeStatus, long newVersion) {
      super(store, storeStatus, newVersion);
    }

    @Override
    public void onRelation(long line, String subject, String predicate, String object, String graph)
        throws LogProcessingFailedException {
      if (predicate.equals(RDF_TYPE)) {
        try {
          put(object, subject);
        } catch (DatabaseException e) {
          throw new LogProcessingFailedException(e);
        }
      }
    }

    @Override
    public void onLiteral(long line, String subject, String predicate, String object, String valueType, String graph)
        throws LogProcessingFailedException { }

    @Override
    public void onLanguageTaggedString(long line, String subject, String predicate, String value, String language,
                                       String graph)
        throws LogProcessingFailedException { }

  }
}
