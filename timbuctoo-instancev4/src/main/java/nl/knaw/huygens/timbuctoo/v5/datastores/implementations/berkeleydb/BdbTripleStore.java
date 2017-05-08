package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatus;
import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatusImpl;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadLoader;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.ProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.util.AutoCloseableIterator;
import org.slf4j.Logger;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.LANGSTRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

public class BdbTripleStore extends BerkeleyStore implements TripleStore {

  protected DatabaseEntry key = new DatabaseEntry();
  protected DatabaseEntry value = new DatabaseEntry();
  protected TripleWriter tripleWriter;
  private static final Logger LOG = getLogger(BdbTripleStore.class);

  public BdbTripleStore(String dataSetName, Environment dbEnvironment, ObjectMapper objectMapper)
      throws DatabaseException {
    super(dbEnvironment, "rdfData_" + dataSetName, objectMapper);
  }

  protected DatabaseConfig getDatabaseConfig() {
    DatabaseConfig rdfConfig = new DatabaseConfig();
    rdfConfig.setAllowCreate(true);
    rdfConfig.setSortedDuplicates(true);
    return rdfConfig;
  }

  @Override
  public void getTriples(QuadHandler handler) throws LogProcessingFailedException {
    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry value = new DatabaseEntry();

    BerkeleyStore.DatabaseFunction getNext = cursor -> cursor.getNext(key, value, LockMode.DEFAULT);
    try (AutoCloseableIterator<String[]> items = getItems(getNext, getNext, () -> formatResult(key, value))) {
      processItems(handler, items);
    }
  }

  @Override
  public AutoCloseableIterator<String[]>  getTriples(String subject, String predicate) {
    if (predicate.equals(RDF_TYPE)) {
      predicate = "";
    }
    binding.objectToEntry(subject + "\n" + predicate, key);
    value.setData(new byte[0]);

    return getItems(
      this::initializer,
      this::iterator,
      () -> formatResult(key, value)
    );
  }

  private void processItems(QuadHandler handler, AutoCloseableIterator<String[]> items)
      throws LogProcessingFailedException {
    boolean ok = true;
    handler.start(0);
    long line = 0;
    while (ok && items.hasNext()) {
      if (Thread.currentThread().isInterrupted()) {
        handler.cancel();
        ok = false;
      }
      String[] qd = items.next();
      if (qd[3] == null) {
        handler.onRelation(line++, qd[0], qd[1], qd[2], qd[4]);
      } else {
        if (qd[3].startsWith(LANGSTRING)) {
          handler.onLanguageTaggedString(line++, qd[0], qd[1], qd[2], qd[3].substring(LANGSTRING.length() + 1), qd[4]);
        } else {
          handler.onLiteral(line++, qd[0], qd[1], qd[2], qd[3], qd[4]);
        }
      }
    }
    handler.finish();
  }

  private OperationStatus iterator(Cursor cursor) throws DatabaseException {
    return cursor.getNextDup(key, value, LockMode.DEFAULT);
  }

  private OperationStatus initializer(Cursor cursor) throws DatabaseException {
    return cursor.getSearchKey(key, value, LockMode.DEFAULT);
  }

  private String[] formatResult(DatabaseEntry key, DatabaseEntry value) {
    String[] result = new String[5];
    String[] keyFields = binding.entryToObject(key).split("\n");
    String[] valueFields = binding.entryToObject(value).split("\n", 2);
    result[0] = keyFields[0];
    result[1] = keyFields.length == 1 ? RDF_TYPE : keyFields[1];
    result[2] = valueFields[1];
    result[3] = valueFields[0].isEmpty() ? null : valueFields[0];
    result[4] = "http://Notsupported";
    return result;
  }

  @Override
  public StoreStatus getStatus() {
    return storeStatus;
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

  private class TripleWriter extends TripleWriterBase {
    public TripleWriter(BdbTripleStore store, StoreStatusImpl storeStatus, long newVersion) {
      super(store, storeStatus, newVersion);
    }

    @Override
    public void onRelation(long line, String subject, String predicate, String object, String graph)
        throws LogProcessingFailedException {
      if (storeStatus.getPosition() > line) {
        return;
      }
      if (predicate.equals(RDF_TYPE)) {
        predicate = "";
      }
      try {
        put(subject + "\n" + predicate, "\n" + object);
        if (!predicate.isEmpty()) {
          put(
            object + "\n" +
              predicate + "_inverse",//FIXME! maybe make this a marker or something? instead of creating a new predicate
            "\n" + subject
          );
        }
        storeStatus.setPosition(line);
      } catch (DatabaseException e) {
        throw new LogProcessingFailedException(e);
      }
    }

    @Override
    public void onLiteral(long line, String subject, String predicate, String object, String valueType, String graph)
        throws LogProcessingFailedException {
      if (storeStatus.getPosition() > line) {
        return;
      }

      try {
        put(subject + "\n" + predicate, valueType + "\n" + object);
        storeStatus.setPosition(line);
      } catch (DatabaseException e) {
        throw new LogProcessingFailedException(e);
      }
    }

    @Override
    public void onLanguageTaggedString(long line, String subject, String predicate, String value, String language,
                                       String graph)
        throws LogProcessingFailedException {
      if (storeStatus.getPosition() > line) {
        return;
      }
      try {
        put(subject + "\n" + predicate, LANGSTRING + "-" + language + "\n" +
          value); //FIXME! store languages properly
        storeStatus.setPosition(line);
      } catch (DatabaseException e) {
        throw new LogProcessingFailedException(e);
      }
    }

  }
}
