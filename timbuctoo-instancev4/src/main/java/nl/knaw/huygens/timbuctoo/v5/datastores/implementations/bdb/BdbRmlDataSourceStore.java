package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.rmldatasource.RmlDataSourceStore;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_HAS_ROW;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_NAME;

public class BdbRmlDataSourceStore implements RmlDataSourceStore {
  protected final BdbWrapper<String, String> bdbWrapper;

  public BdbRmlDataSourceStore(BdbWrapper<String, String> bdbWrapper)
    throws DataStoreCreationException {
    this.bdbWrapper = bdbWrapper;
  }

  @Override
  public Stream<String> get(String collectionUri) {
    return bdbWrapper.databaseGetter()
      .key(collectionUri)
      .dontSkip()
      .forwards()
      .getValues();
  }

  public void put(String key, String value) throws RdfProcessingFailedException {
    try {
      bdbWrapper.put(key, value);
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e.getCause());
    }
  }

  private static class RdfHandler implements RdfProcessor {
    private final BdbRmlDataSourceStore rmlDataSourceStore;
    private Map<String, String> predicates;
    private int currentVersion = -1;

    public RdfHandler(BdbRmlDataSourceStore rmlDataSourceStore) {
      this.rmlDataSourceStore = rmlDataSourceStore;
      this.predicates = new HashMap<>(); //FIXME: assumption on order of RDF
    }

    @Override
    public void setPrefix(String prefix, String iri) throws RdfProcessingFailedException {

    }

    @Override
    public void addRelation(String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException {
      if (TIM_HAS_ROW.equals(predicate)) {
        rmlDataSourceStore.put(object, subject);
      }
    }

    @Override
    public void addValue(String subject, String predicate, String value, String dataType, String graph)
      throws RdfProcessingFailedException {
      if (predicates.containsKey(predicate)) {
        rmlDataSourceStore.put(subject, StringEscapeUtils.escapeJava(predicates.get(predicate)) + "\n" + value);
      } else if (TIM_PROP_NAME.equals(predicate)) {
        predicates.put(subject, value);
      }
    }

    @Override
    public void addLanguageTaggedString(String subject, String predicate, String value, String language,
                                        String graph) throws RdfProcessingFailedException {
      if (predicates.containsKey(predicate)) {
        rmlDataSourceStore.put(subject, StringEscapeUtils.escapeJava(predicates.get(predicate)) + "\n" + value);
      } else if (TIM_PROP_NAME.equals(predicate)) {
        predicates.put(subject, value);
      }
    }

    // delete is basically not used, so these methods will not be implemented for now.

    @Override
    public void delRelation(String subject, String predicate, String object, String graph)
      throws RdfProcessingFailedException { }

    @Override
    public void delValue(String subject, String predicate, String value, String valueType, String graph)
      throws RdfProcessingFailedException { }

    @Override
    public void delLanguageTaggedString(String subject, String predicate, String value, String language,
                                        String graph) throws RdfProcessingFailedException { }

    @Override
    public void start(int index) throws RdfProcessingFailedException {

      currentVersion = index;
    }

    @Override
    public int getCurrentVersion() {
      return currentVersion;
    }

    @Override
    public void commit() throws RdfProcessingFailedException { }
  }
}
