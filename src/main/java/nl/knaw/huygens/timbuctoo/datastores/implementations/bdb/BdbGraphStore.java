package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.datastores.graphstore.GraphStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.berkeleydb.DatabaseGetter.Iterate.BACKWARDS;
import static nl.knaw.huygens.timbuctoo.berkeleydb.DatabaseGetter.Iterate.FORWARDS;

class BdbGraphStore implements GraphStore {
  private static final Logger LOG = LoggerFactory.getLogger(BdbGraphStore.class);
  private final BdbWrapper<String, String> bdbWrapper;

  BdbGraphStore(BdbWrapper<String, String> bdbWrapper) {
    this.bdbWrapper = bdbWrapper;
  }

  @Override
  public void put(String graph, String subject) throws RdfProcessingFailedException {
    try {
      if (graph != null && !graph.isBlank()) {
        bdbWrapper.put(graph, subject);
      }
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public void delete(String graph, String subject) throws RdfProcessingFailedException {
    try {
      if (graph != null && !graph.isBlank()) {
        bdbWrapper.delete(graph, subject);
      }
    } catch (DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }

  @Override
  public Stream<String> ofGraph(String graph) {
    return bdbWrapper.databaseGetter().key(graph).dontSkip().forwards().getValues(bdbWrapper.valueRetriever());
  }

  @Override
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

  @Override
  public void close() {
    try {
      bdbWrapper.close();
    } catch (Exception e) {
      LOG.error("Exception closing GraphStore", e);
    }
  }

  @Override
  public void commit() {
    bdbWrapper.commit();
  }

  @Override
  public void start() {
    bdbWrapper.beginTransaction();
  }

  @Override
  public boolean isClean() {
    return bdbWrapper.isClean();
  }

  @Override
  public void empty() {
    bdbWrapper.empty();
  }
}
