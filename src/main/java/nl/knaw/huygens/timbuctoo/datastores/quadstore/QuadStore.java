package nl.knaw.huygens.timbuctoo.datastores.quadstore;

import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.QuadGraphs;
import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.Optional;
import java.util.stream.Stream;

public interface QuadStore {
  Stream<CursorQuad> getQuads(String subject);

  Stream<CursorQuad> getQuads(String subject, String predicate, Direction direction, String cursor);

  Stream<CursorQuad> getQuads(String subject, String predicate, Direction direction, String cursor, boolean skipGraphs);

  Stream<CursorQuad> getQuadsInGraph(String subject, Optional<Graph> graph);

  Stream<CursorQuad> getQuadsInGraph(String subject, String predicate, Direction direction,
                                     String cursor, Optional<Graph> graph);

  Stream<CursorQuad> getQuadsInGraph(String subject, String predicate, Direction direction,
                                     String cursor, Optional<Graph> graph, boolean skipGraphs);

  Stream<CursorQuad> getAllQuads();

  Stream<CursorQuad> getAllQuadsInGraph(Optional<Graph> graph);

  void close();

  static String formatKey(String subject, String predicate, Direction direction) {
    return subject + "\n" + predicate + "\n" + (direction == null ? "" : direction.name());
  }

  static String formatValue(String object, String dataType, String language, String graph) {
    return (dataType == null ? "" : dataType) + "\n" + (language == null ? "" : language) + "\n" +
        object + "\n" + (graph == null ? "" : graph);
  }

  static String format(CursorQuad quad) {
    return formatKey(quad.getSubject(), quad.getPredicate(), quad.getDirection()) + "\n" +
        formatValue(quad.getObject(), quad.getValuetype().orElse(null),
            quad.getLanguage().orElse(null), quad.getGraph().orElse(null));
  }

  static String format(QuadGraphs quad) {
    return formatKey(quad.getSubject(), quad.getPredicate(), quad.getDirection()) + "\n" +
        formatValue(quad.getObject(), quad.getValuetype().orElse(null),
            quad.getLanguage().orElse(null), null);
  }

  static int compare(CursorQuad leftQ, CursorQuad rightQ) {
    final String leftStr = format(leftQ);
    final String rightStr = format(rightQ);
    return leftStr.compareTo(rightStr);
  }

  static int compare(QuadGraphs leftQ, QuadGraphs rightQ) {
    final String leftStr = format(leftQ);
    final String rightStr = format(rightQ);
    return leftStr.compareTo(rightStr);
  }

  void commit();

  boolean isClean();

  void start();

  boolean putQuad(String subject, String predicate, Direction direction, String object, String dataType,
                  String language, String graph) throws RdfProcessingFailedException;

  boolean deleteQuad(String subject, String predicate, Direction direction, String object, String valueType,
                     String language, String graph) throws RdfProcessingFailedException;

  void empty();
}
