package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.DatabaseGetter;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.QuadGraphs;
import nl.knaw.huygens.timbuctoo.v5.util.Graph;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.berkeleydb.DatabaseGetter.Iterate.BACKWARDS;
import static nl.knaw.huygens.timbuctoo.v5.berkeleydb.DatabaseGetter.Iterate.FORWARDS;

/**
 * This datastore determines the current state of the DataSet.
 */
public class BdbQuadStore implements QuadStore {
  private static final Logger LOG = LoggerFactory.getLogger(BdbQuadStore.class);
  private final BdbWrapper<String, String> bdbWrapper;

  public BdbQuadStore(BdbWrapper<String, String> rdfData)
    throws DataStoreCreationException {
    this.bdbWrapper = rdfData;
  }

  @Override
  public Stream<CursorQuad> getQuads(String subject) {
    return getQuadsInGraph(subject, Optional.empty());
  }

  @Override
  public Stream<CursorQuad> getQuads(String subject, String predicate, Direction direction, String cursor) {
    return getQuadsInGraph(subject, predicate, direction, cursor, Optional.empty());
  }

  @Override
  public Stream<CursorQuad> getQuadsInGraph(String subject, Optional<Graph> graph) {
    return bdbWrapper.databaseGetter()
                     .partialKey(subject + "\n", (prefix, key) -> key.startsWith(prefix))
                     .dontSkip()
                     .forwards()
                     .getKeysAndValues(bdbWrapper.keyValueConverter(this::formatResult))
                     .filter(quad -> quad.inGraph(graph));
  }

  @Override
  public Stream<CursorQuad> getQuadsInGraph(String subject, String predicate,
                                            Direction direction, String cursor, Optional<Graph> graph) {
    final DatabaseGetter<String, String> getter;
    if (cursor.isEmpty()) {
      getter = bdbWrapper.databaseGetter()
                         .key((formatKey(subject, predicate, direction)))
                         .dontSkip()
                         .forwards();
    } else {
      if (cursor.equals("LAST")) {
        getter = bdbWrapper.databaseGetter()
                           .key((formatKey(subject, predicate, direction)))
                           .skipToEnd()
                           .backwards();
      } else {
        String[] fields = cursor.substring(2).split("\n\n", 2);
        getter = bdbWrapper.databaseGetter()
                           .key(fields[0])
                           .skipToValue(fields[1])
                           .skipOne() //we start after the cursor
                           .direction(cursor.startsWith("A\n") ? FORWARDS : BACKWARDS);
      }
    }

    return getter.getKeysAndValues(bdbWrapper.keyValueConverter(this::formatResult))
                 .filter(quad -> quad.inGraph(graph));
  }

  @Override
  public Stream<CursorUri> getSubjectsInCollection(String collectionUri, String cursor) {
    return getSubjectsInCollectionInGraph(collectionUri, cursor, Optional.empty());
  }

  @Override
  public Stream<CursorUri> getSubjectsInCollectionInGraph(String collectionUri, String cursor, Optional<Graph> graph) {
    DatabaseGetter.Iterate direction = cursor.isEmpty() || cursor.startsWith("A\n") ? FORWARDS : BACKWARDS;

    final DatabaseGetter.PrimedBuilder<String, String> getter;
    if (cursor.equals("LAST")) {
      getter = bdbWrapper.databaseGetter()
                         .key((formatKey(collectionUri, RdfConstants.RDF_TYPE, Direction.IN)))
                         .skipToEnd();
    } else {
      getter = bdbWrapper.databaseGetter()
                         .key((formatKey(collectionUri, RdfConstants.RDF_TYPE, Direction.IN)))
                         .dontSkip();
    }

    Stream<CursorUri> result = getter
        .direction(direction)
        .getKeysAndValues(bdbWrapper.keyValueConverter(this::formatResult))
        .filter(quad -> quad.inGraph(graph))
        .map(quad -> CursorUri.create(quad.getObject()))
        .distinct();

    if (cursor.isEmpty()) {
      return result;
    }

    return result
        .dropWhile(cursorUri -> !cursorUri.getUri().equals(cursor.substring(2)))
        .skip(1); //we start after the cursor
  }

  @Override
  public Stream<CursorQuad> getAllQuads() {
    return getAllQuadsInGraph(Optional.empty());
  }

  @Override
  public Stream<CursorQuad> getAllQuadsInGraph(Optional<Graph> graph) {
    return bdbWrapper.databaseGetter()
                     .getAll()
                     .getKeysAndValues(bdbWrapper.keyValueConverter(this::formatResult))
                     .filter(quad -> quad.inGraph(graph));
  }

  @Override
  public void close() {
    try {
      bdbWrapper.close();
    } catch (Exception e) {
      LOG.error("Exception closing BdbQuadStore", e);
    }
  }

  private CursorQuad formatResult(String key, String value) {
    String cursor = key + "\n\n" + value;
    String[] keyFields = key.split("\n", 3);
    String[] valueFields = value.split("\n", 4);
    return CursorQuad.create(
      keyFields[0],
      keyFields[1],
      Direction.valueOf(keyFields[2]),
      valueFields[3],
      valueFields[0].isEmpty() ? null : valueFields[0],
      valueFields[1].isEmpty() ? null : valueFields[1],
      valueFields[2].isEmpty() ? null : valueFields[2],
      cursor
    );
  }

  public boolean putQuad(String subject, String predicate, Direction direction, String object, String dataType,
                         String language, String graph) throws DatabaseWriteException {
    String value = formatValue(object, dataType, language, graph);
    return bdbWrapper.put(formatKey(subject, predicate, direction), value);
  }

  public boolean deleteQuad(String subject, String predicate, Direction direction, String object, String dataType,
                         String language, String graph) throws DatabaseWriteException {
    String value = formatValue(object, dataType, language, graph);
    return bdbWrapper.delete(formatKey(subject, predicate, direction), value);
  }

  public static String formatKey(String subject, String predicate, Direction direction) {
    return subject + "\n" + predicate + "\n" + (direction == null ? "" : direction.name());
  }

  public static String formatValue(String object, String dataType, String language, String graph) {
    return (dataType == null ? "" : dataType) + "\n" + (language == null ? "" : language) + "\n" +
        (graph == null ? "" : graph) + "\n" + object;
  }

  public static String format(CursorQuad quad) {
    return formatKey(quad.getSubject(), quad.getPredicate(), quad.getDirection()) + "\n" +
        formatValue(quad.getObject(), quad.getValuetype().orElse(null),
            quad.getLanguage().orElse(null), quad.getGraph().orElse(null));
  }

  public static String format(QuadGraphs quad) {
    return formatKey(quad.getSubject(), quad.getPredicate(), quad.getDirection()) + "\n" +
        formatValue(quad.getObject(), quad.getValuetype().orElse(null),
            quad.getLanguage().orElse(null), null);
  }

  public static int compare(CursorQuad leftQ, CursorQuad rightQ) {
    final String leftStr = format(leftQ);
    final String rightStr = format(rightQ);
    return leftStr.compareTo(rightStr);
  }

  public static int compare(QuadGraphs leftQ, QuadGraphs rightQ) {
    final String leftStr = format(leftQ);
    final String rightStr = format(rightQ);
    return leftStr.compareTo(rightStr);
  }

  @Override
  public void commit() {
    bdbWrapper.commit();
  }

  @Override
  public boolean isClean() {
    return bdbWrapper.isClean();
  }

  public void start() {
    bdbWrapper.beginTransaction();
  }

  public void empty() {
    bdbWrapper.empty();
  }
}
