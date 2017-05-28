package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import com.google.common.base.Charsets;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class BdbTripleStore extends BerkeleyStore implements TripleStore {

  protected DatabaseEntry key;
  protected DatabaseEntry value;
  private String prefix;

  public BdbTripleStore(String dataSetName, Environment dbEnvironment) throws DatabaseException {
    super(dbEnvironment, "rdfData_" + dataSetName);
  }

  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig rdfConfig = new DatabaseConfig();
    rdfConfig.setAllowCreate(true);
    rdfConfig.setSortedDuplicates(true);
    return rdfConfig;
  }

  @Override
  public AutoCloseableIterator<String[]> getTriples() {
    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry value = new DatabaseEntry();

    BerkeleyStore.DatabaseFunction getNext = cursor -> cursor.getNext(key, value, LockMode.DEFAULT);
    return getItems(getNext, getNext, () -> formatResult(key, value));
  }

  @Override
  public AutoCloseableIterator<String[]> getTriples(String subject, String predicate) {
    if (predicate.equals(RDF_TYPE)) {
      predicate = "";
    }
    key = new DatabaseEntry((subject + "\n" + predicate).getBytes(Charsets.UTF_8));
    value = new DatabaseEntry();

    return getItems(
      this::initializer,
      this::iterator,
      () -> formatResult(key, value)
    );
  }

  private OperationStatus iterator(Cursor cursor) throws DatabaseException {
    return cursor.getNextDup(key, value, LockMode.DEFAULT);
  }

  private OperationStatus initializer(Cursor cursor) throws DatabaseException {
    return cursor.getSearchKey(key, value, LockMode.DEFAULT);
  }

  private String[] formatResult(DatabaseEntry key, DatabaseEntry value) {
    String[] result = new String[5];
    String[] keyFields = new String(key.getData(), Charsets.UTF_8).split("\n");
    String[] valueFields = new String(value.getData(), Charsets.UTF_8).split("\n", 2);
    result[0] = keyFields[0];
    result[1] = keyFields.length == 1 ? RDF_TYPE : keyFields[1];
    result[2] = valueFields[1];
    result[3] = valueFields[0].isEmpty() ? null : valueFields[0];
    result[4] = "http://Notsupported";
    return result;
  }

  @Override
  public String getStatus() {
    return null;
  }

  @Override
  public void setPrefix(String cursor, String prefix, String iri) throws RdfProcessingFailedException {

  }

  @Override
  public void addRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
    if (predicate.equals(RDF_TYPE)) {
      predicate = "";
    }
    try {
      put(
        subject + "\n" +
          predicate,
          "" + "\n" +
          object
      );
      if (!predicate.isEmpty()) {
        put(
          object + "\n" +
            predicate + "_inverse",
          "\n" + subject
        );
      }
    } catch (DatabaseException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void addValue(String cursor, String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {
    try {
      put(
        subject + "\n" +
          predicate,
          dataType + "\n" +
          value
      );
    } catch (DatabaseException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void addLanguageTaggedString(String cursor, String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {
    try {
      put(
        subject + "\n" +
          predicate,
        RdfConstants.LANGSTRING + "_" + language + "\n" +
          value
      );
    } catch (DatabaseException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void delRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {}

  @Override
  public void delValue(String cursor, String subject, String predicate, String value, String valueType, String graph)
      throws RdfProcessingFailedException {}

  @Override
  public void delLanguageTaggedString(String cursor, String subject, String predicate, String value, String language,
                                      String graph) throws RdfProcessingFailedException {}
}
