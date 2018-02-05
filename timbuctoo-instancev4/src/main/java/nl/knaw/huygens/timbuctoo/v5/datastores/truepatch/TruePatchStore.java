package nl.knaw.huygens.timbuctoo.v5.datastores.truepatch;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;

import java.util.stream.Stream;

public interface TruePatchStore {
  void put(String subject, int currentversion, String predicate, Direction direction, boolean isAssertion,
           String object, String valueType, String language) throws RdfProcessingFailedException;

  Stream<CursorQuad> getChanges(String subject, int version, boolean assertions);

  Stream<CursorQuad> getChanges(String subject, String predicate, Direction direction, int version,
                                boolean assertions);

  CursorQuad makeCursorQuad(String subject, boolean assertions, String value);

  void close();

  void commit();

  void start();

  boolean isClean();

  void empty();
}
