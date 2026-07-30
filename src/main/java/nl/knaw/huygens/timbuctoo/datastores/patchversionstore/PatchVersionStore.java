package nl.knaw.huygens.timbuctoo.datastores.patchversionstore;

import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;

import java.util.stream.Stream;

public interface PatchVersionStore {
  void put(String subject, String predicate, Direction direction, boolean isAssertion,
           String object, String valueType, String language, String graph) throws RdfProcessingFailedException;

  Stream<CursorQuad> getAllChanges(boolean assertions);

  Stream<CursorQuad> getChanges(String subject, boolean assertions);

  Stream<CursorQuad> getChanges(String subject, String predicate, Direction direction, boolean assertions);

  Stream<CursorQuad> retrieveChanges();

  CursorQuad makeCursorQuad(String subject, boolean assertions, String value);

  void close();

  void commit();

  void start();

  boolean isClean();

  void empty();
}
