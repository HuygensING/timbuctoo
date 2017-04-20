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
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;

public class BdbTripleStore extends BerkeleyStore implements TripleStore {

  protected DatabaseEntry key;
  protected DatabaseEntry value;
  private String prefix;

  public BdbTripleStore(String dataSetName, Environment dbEnvironment) throws DatabaseException {
    super(dbEnvironment, "rdfData_" + dataSetName);
  }

  @Override
  public void onPrefix(String prefix, String uri) {}

  @Override
  public void onQuad(String subject, String predicate, String object, String valueType, String graph)
      throws LogProcessingFailedException {

    if (predicate.equals(RDF_TYPE)) {
      predicate = "";
    }
    try {
      put(
        subject + "\n" +
          predicate,
          (valueType != null ? valueType : "") + "\n" +
            object
      );
      if (valueType == null && !predicate.isEmpty()) {
        put(
          object + "\n" +
            predicate + "_inverse",
          "\n" + subject
        );
      }
    } catch (DatabaseException e) {
      throw new LogProcessingFailedException(e);
    }
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

}
