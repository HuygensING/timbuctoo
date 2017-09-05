package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.sleepycat.je.DatabaseConfig;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.DatabaseGetter;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CursorSubject;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

// Rdf processor that only handles the rdf:type (relation) triples
public class BdbCollectionIndex extends BerkeleyStore implements RdfProcessor, AutoCloseable, CollectionIndex {
  public BdbCollectionIndex(DataProvider dataProvider, BdbDatabaseCreator factory, String userId, String dataSetId)
    throws DataStoreCreationException {
    super(factory, "collectionIndex", userId, dataSetId);
    dataProvider.subscribeToRdf(this, 0);
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
  public void setPrefix(String prefix, String iri) {}

  @Override
  public void addRelation(String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
    if (predicate.equals(RDF_TYPE)) {
      try {
        bdbWrapper.put(transaction, object, subject);
      } catch (DatabaseWriteException e) {
        throw new RdfProcessingFailedException(e.getCause());
      }
    }
  }

  @Override
  public void delRelation(String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
    if (predicate.equals(RDF_TYPE)) {
      try {
        bdbWrapper.delete(transaction, object, subject);
      } catch (DatabaseWriteException e) {
        throw new RdfProcessingFailedException(e.getCause());
      }
    }
  }


  @Override
  public void addValue(String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {
    // no implementation needed
  }

  @Override
  public void addLanguageTaggedString(String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {
    // no implementation needed
  }

  @Override
  public void delValue(String subject, String predicate, String value, String valueType, String graph)
      throws RdfProcessingFailedException {
    // no implementation needed
  }

  @Override
  public void delLanguageTaggedString(String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {
    // no implementation needed
  }

  @Override
  public Stream<CursorSubject> getSubjects(String collectionName, String cursor) {
    final DatabaseGetter<String> getter;
    if (cursor.isEmpty()) {
      getter = bdbWrapper.databaseGetter()
        .startAtKey(collectionName)
        .getAllWithSameKey(true);
    } else {
      if ("LAST".equals(cursor)) {
        getter = bdbWrapper.databaseGetter()
          .startAtEndOfKeyDuplicates(collectionName)
          .getAllWithSameKey(false);
      } else {
        //initializer starts at the cursor.substring(2)
        getter = bdbWrapper.databaseGetter()
          .startAfterValue(collectionName, cursor.substring(2))
          .getAllWithSameKey(cursor.startsWith("A\n"));
      }
    }

    return getter.getValues().map(s -> CursorSubject.create(s, s));
  }

}
