package nl.knaw.huygens.timbuctoo.v5.datastores.quadstore;

import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;

import java.util.stream.Stream;

public interface QuadStore {
  Stream<CursorQuad> getQuads(String subject, String predicate, Direction direction, String cursor);

  Stream<CursorQuad> getQuads(String subject);

  void close();

  int compare(CursorQuad leftQ, CursorQuad rightQ);

  void commit();

  boolean isClean();

  void start();

  boolean putQuad(String subject, String predicate, Direction direction, String object, String dataType,
                  String language) throws DatabaseWriteException;

  boolean deleteQuad(String subject, String predicate, Direction direction, String object, String valueType,
                     String language) throws DatabaseWriteException;

  void empty();
}
