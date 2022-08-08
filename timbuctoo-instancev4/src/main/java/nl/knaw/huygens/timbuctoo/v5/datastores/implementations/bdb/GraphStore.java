package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.berkeleydb.DatabaseGetter.Iterate.BACKWARDS;
import static nl.knaw.huygens.timbuctoo.v5.berkeleydb.DatabaseGetter.Iterate.FORWARDS;

public class GraphStore {
  private static final Logger LOG = LoggerFactory.getLogger(GraphStore.class);
  private final BdbWrapper<String, String> bdbWrapper;

  public GraphStore(BdbWrapper<String, String> bdbWrapper)
      throws DataStoreCreationException {
    this.bdbWrapper = bdbWrapper;
  }

  public void put(String graph, String subject) throws DatabaseWriteException {
    if (graph != null && !graph.isBlank()) {
      bdbWrapper.put(graph, subject);
    }
  }

  public void delete(String graph, String subject) throws DatabaseWriteException {
    if (graph != null && !graph.isBlank()) {
      bdbWrapper.delete(graph, subject);
    }
  }

  public Stream<String> ofGraph(String graph) {
    return bdbWrapper.databaseGetter().key(graph).dontSkip().forwards().getValues(bdbWrapper.valueRetriever());
  }

  public Stream<CursorUri> getGraphs(String cursor) {
    if (cursor.isEmpty()) {
      return bdbWrapper.databaseGetter().getAll().getKeys(bdbWrapper.keyRetriever())
                       .distinct().map(CursorUri::create);
    }

    return bdbWrapper.databaseGetter()
                     .skipToKey(cursor.substring(2))
                     .skipToEnd()
                     .skipOne() //we start after the cursor
                     .direction(cursor.startsWith("A\n") ? FORWARDS : BACKWARDS)
                     .getKeys(bdbWrapper.keyRetriever())
                     .distinct()
                     .map(CursorUri::create);
  }

  public void close() {
    try {
      bdbWrapper.close();
    } catch (Exception e) {
      LOG.error("Exception closing GraphStore", e);
    }
  }

  public void commit() {
    bdbWrapper.commit();
  }

  public void start() {
    bdbWrapper.beginTransaction();
  }

  public boolean isClean() {
    return bdbWrapper.isClean();
  }

  public void empty() {
    bdbWrapper.empty();
  }
}
