package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatus;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatusImpl;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadLoader;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.ProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;
import org.slf4j.Logger;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

public class BdbCollectionIndex extends BerkeleyStore implements CollectionIndex {
  private TripleWriter tripleWriter;
  private static final Logger LOG = getLogger(BdbCollectionIndex.class);


  public BdbCollectionIndex(String dataSetName, Environment dbEnvironment, ObjectMapper objectMapper)
      throws DatabaseException {
    super(dbEnvironment, "collectionIndex_" + dataSetName, objectMapper);
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
  public AutoCloseableIterator<String> getSubjects(String collectionName) {
    DatabaseEntry key = new DatabaseEntry();
    binding.objectToEntry(collectionName, key);
    DatabaseEntry value = new DatabaseEntry();

    return getItems(
      cursor -> cursor.getSearchKey(key, value, LockMode.DEFAULT),
      cursor -> cursor.getNextDup(key, value, LockMode.DEFAULT),
      () -> binding.entryToObject(value)
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
        throws LogProcessingFailedException {
    }

    @Override
    public void onLanguageTaggedString(long line, String subject, String predicate, String value, String language,
                                       String graph)
        throws LogProcessingFailedException {
    }

  }
}
