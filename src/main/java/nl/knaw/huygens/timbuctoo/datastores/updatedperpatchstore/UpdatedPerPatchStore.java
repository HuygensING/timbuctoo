package nl.knaw.huygens.timbuctoo.datastores.updatedperpatchstore;

import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;

import java.util.stream.Stream;

public interface UpdatedPerPatchStore {
  void put(int currentversion, String subject) throws RdfProcessingFailedException;

  Stream<String> ofVersion(int version);

  Stream<SubjectCursor> fromVersion(int version, String cursor);

  Stream<Integer> getVersions();

  void close();

  void commit();

  void start();

  boolean isClean();

  void empty();
}

