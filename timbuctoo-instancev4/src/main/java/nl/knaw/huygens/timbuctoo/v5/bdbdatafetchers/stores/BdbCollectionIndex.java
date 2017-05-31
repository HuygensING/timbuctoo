package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores;

import com.google.common.base.Charsets;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorSubject;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.BdbDatabaseFactory;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class BdbCollectionIndex extends BerkeleyStore implements RdfProcessor, AutoCloseable {
  public BdbCollectionIndex(DataSet dataSet, BdbDatabaseFactory factory, String userId, String dataSetId)
    throws DataStoreCreationException {
    super(factory, "collectionIndex", userId, dataSetId);
    dataSet.subscribeToRdf(this, null);
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

  public Stream<CursorSubject> getSubjects(String collectionName) {
    DatabaseEntry key = new DatabaseEntry(collectionName.getBytes(Charsets.UTF_8));
    DatabaseEntry value = new DatabaseEntry();

    return getItems(
      cursor -> cursor.getSearchKey(key, value, LockMode.DEFAULT),
      cursor -> cursor.getNextDup(key, value, LockMode.DEFAULT),
      () -> CursorSubject.create("", new String(value.getData(), Charsets.UTF_8))
    );
  }
}
