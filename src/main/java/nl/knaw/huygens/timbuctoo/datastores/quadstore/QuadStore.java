package nl.knaw.huygens.timbuctoo.datastores.quadstore;

import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
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

  void commit();

  boolean isClean();
}
