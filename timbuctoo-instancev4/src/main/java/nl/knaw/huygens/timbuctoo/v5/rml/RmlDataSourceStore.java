package nl.knaw.huygens.timbuctoo.v5.rml;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_HAS_ROW;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_NAME;

public class RmlDataSourceStore {
  protected final BdbWrapper bdbWrapper;
  protected final EntryBinding<String> binder = TupleBinding.getPrimitiveBinding(String.class);
  protected Transaction transaction;
  private final DatabaseEntry key = new DatabaseEntry();
  private final DatabaseEntry value = new DatabaseEntry();

  public RmlDataSourceStore(String userId, String dataSetId, BdbDatabaseCreator dbCreator, DataProvider dataSet)
    throws DataStoreCreationException {
    bdbWrapper = dbCreator.getDatabase(userId, dataSetId, "rmlSource", getConfig());
    dataSet.subscribeToRdf(new RdfHandler(this), null);
  }

  private DatabaseConfig getConfig() {
    DatabaseConfig databaseConfig = new DatabaseConfig();
    databaseConfig.setAllowCreate(true);
    databaseConfig.setSortedDuplicates(true);
    return databaseConfig;
  }

  public Stream<String> get(String collectionUri) {
    DatabaseEntry key = new DatabaseEntry();
    DatabaseEntry value = new DatabaseEntry();
    binder.objectToEntry(collectionUri, key);
    return bdbWrapper.getItems(
      cursor -> cursor.getSearchKey(key, value, LockMode.DEFAULT),
      cursor -> cursor.getNextDup(key, value, LockMode.DEFAULT),
      () -> binder.entryToObject(value)
    );
  }

  public void put(String key, String value) {
    synchronized (this.key) {
      binder.objectToEntry(key, this.key);
      binder.objectToEntry(value, this.value);
      bdbWrapper.put(transaction, this.key, this.value);
    }
  }

  private static class RdfHandler implements RdfProcessor {
    private final RmlDataSourceStore rmlDataSourceStore;
    private Map<String, String> predicates;

    public RdfHandler(RmlDataSourceStore rmlDataSourceStore) {
      this.rmlDataSourceStore = rmlDataSourceStore;
      this.predicates = new HashMap<>(); //FIXME: assumption on order of RDF
    }

    @Override
    public void setPrefix(String cursor, String prefix, String iri) throws RdfProcessingFailedException {

    }

    @Override
    public void addRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
      if (TIM_HAS_ROW.equals(predicate)) {
        rmlDataSourceStore.put(object, subject);
      }
    }

    @Override
    public void addValue(String cursor, String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {
      if (predicates.containsKey(predicate)) {
        rmlDataSourceStore.put(subject, StringEscapeUtils.escapeJava(predicates.get(predicate)) + "\n" + value);
      } else if (TIM_PROP_NAME.equals(predicate)) {
        predicates.put(subject, value);
      }
    }

    @Override
    public void addLanguageTaggedString(String cursor, String subject, String predicate, String value, String language,
                                        String graph) throws RdfProcessingFailedException {
      if (predicates.containsKey(predicate)) {
        rmlDataSourceStore.put(subject, StringEscapeUtils.escapeJava(predicates.get(predicate)) + "\n" + value);
      } else if (TIM_PROP_NAME.equals(predicate)) {
        predicates.put(subject, value);
      }
    }

    // delete is basically not used, so these methods will not be implemented for now.

    @Override
    public void delRelation(String cursor, String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException { }

    @Override
    public void delValue(String cursor, String subject, String predicate, String value, String valueType, String graph)
      throws RdfProcessingFailedException { }

    @Override
    public void delLanguageTaggedString(String cursor, String subject, String predicate, String value, String language,
                                        String graph) throws RdfProcessingFailedException { }

    @Override
    public void start() throws RdfProcessingFailedException { }

    @Override
    public void finish() throws RdfProcessingFailedException { }
  }
}
