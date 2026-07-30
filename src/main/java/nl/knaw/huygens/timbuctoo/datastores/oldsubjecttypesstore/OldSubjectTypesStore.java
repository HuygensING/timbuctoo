package nl.knaw.huygens.timbuctoo.datastores.oldsubjecttypesstore;

import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.datastores.updatedperpatchstore.SubjectCursor;

import java.util.stream.Stream;

public interface OldSubjectTypesStore {
  long size();

  void put(String subject, String type, int version) throws RdfProcessingFailedException;

  void delete(String subject, String type, int version) throws RdfProcessingFailedException;

  Stream<SubjectCursor> fromTypeAndVersion(String type, int version, String cursor);

  void close();

  void commit();

  void start();

  boolean isClean();

  void empty();
}
