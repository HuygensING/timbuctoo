package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.berkeleydb.DatabaseGetter;
import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.QuadGraphs;
import nl.knaw.huygens.timbuctoo.util.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.berkeleydb.DatabaseGetter.Iterate.BACKWARDS;
import static nl.knaw.huygens.timbuctoo.berkeleydb.DatabaseGetter.Iterate.FORWARDS;

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
    return getQuadsInGraph(subject, predicate, direction, cursor, Optional.empty(), false);
  }

  @Override
  public Stream<CursorQuad> getQuads(String subject, String predicate, Direction direction,
                                     String cursor, boolean skipGraphs) {
    return getQuadsInGraph(subject, predicate, direction, cursor, Optional.empty(), skipGraphs);
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
  public Stream<CursorQuad> getQuadsInGraph(String subject, String predicate, Direction direction,
                                            String cursor, Optional<Graph> graph) {
    return getQuadsInGraph(subject, predicate, direction, cursor, graph, false);
  }

  @Override
  public Stream<CursorQuad> getQuadsInGraph(String subject, String predicate, Direction direction,
                                            String cursor, Optional<Graph> graph, boolean skipGraphs) {
    final CursorQuad start;
    final DatabaseGetter<String, String> getter;
    if (cursor.isEmpty()) {
      start = null;
      getter = bdbWrapper.databaseGetter()
                         .key((formatKey(subject, predicate, direction)))
                         .dontSkip()
                         .forwards();
    } else if (cursor.equals("LAST")) {
      start = null;
      getter = bdbWrapper.databaseGetter()
                         .key((formatKey(subject, predicate, direction)))
                         .skipToEnd()
                         .backwards();
    } else {
      final String[] fields = cursor.substring(2).split("\n\n", 2);
      start = formatResult(fields[0], fields[1]);
      getter = bdbWrapper.databaseGetter()
                         .key(fields[0])
                         .skipToValue(fields[1])
                         .skipOne() //we start after the cursor
                         .direction(cursor.startsWith("A\n") ? FORWARDS : BACKWARDS);
    }

    return getter.getKeysAndValues(bdbWrapper.keyValueConverter(this::formatResult))
                 .dropWhile(quad -> skipGraphs && start != null && start.equalsExcludeGraph(quad))
                 .filter(quad -> quad.inGraph(graph));
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
    String[] valueFields = value.split("\n", 3);
    int objectGraphIdx = valueFields[2].lastIndexOf('\n');
    return CursorQuad.create(
      keyFields[0],
      keyFields[1],
      Direction.valueOf(keyFields[2]),
      valueFields[2].substring(0, objectGraphIdx),
      valueFields[0].isEmpty() ? null : valueFields[0],
      valueFields[1].isEmpty() ? null : valueFields[1],
      valueFields[2].substring(objectGraphIdx + 1).isEmpty() ?
          null : valueFields[2].substring(objectGraphIdx + 1),
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

  private static String formatKey(String subject, String predicate, Direction direction) {
    return subject + "\n" + predicate + "\n" + (direction == null ? "" : direction.name());
  }

  public static String formatValue(String object, String dataType, String language, String graph) {
    return (dataType == null ? "" : dataType) + "\n" + (language == null ? "" : language) + "\n" +
        object + "\n" + (graph == null ? "" : graph);
  }

  private static String format(CursorQuad quad) {
    return formatKey(quad.getSubject(), quad.getPredicate(), quad.getDirection()) + "\n" +
        formatValue(quad.getObject(), quad.getValuetype().orElse(null),
            quad.getLanguage().orElse(null), quad.getGraph().orElse(null));
  }

  private static String format(QuadGraphs quad) {
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
