package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import com.google.common.base.Charsets;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class BdbCollectionIndex extends BerkeleyStore implements CollectionIndex {
  public BdbCollectionIndex(String dataSetName, Environment dbEnvironment) throws DatabaseException {
    super(dbEnvironment, "collectionIndex_" + dataSetName);
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
  public void onPrefix(String prefix, String uri) {}

  @Override
  public void onQuad(String subject, String predicate, String object, String valueType,
                     String graph)
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
  public AutoCloseableIterator<String> getSubjects(String collectionName) {
    DatabaseEntry key = new DatabaseEntry(collectionName.getBytes(Charsets.UTF_8));
    DatabaseEntry value = new DatabaseEntry();

    return getItems(
      cursor -> cursor.getSearchKey(key, value, LockMode.DEFAULT),
      cursor -> cursor.getNextDup(key, value, LockMode.DEFAULT),
      () -> new String(value.getData(), Charsets.UTF_8)
    );
  }
}
