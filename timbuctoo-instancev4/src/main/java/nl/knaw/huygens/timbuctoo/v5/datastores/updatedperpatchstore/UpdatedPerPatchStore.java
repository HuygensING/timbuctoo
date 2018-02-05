package nl.knaw.huygens.timbuctoo.v5.datastores.updatedperpatchstore;

import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;

import java.util.stream.Stream;

public interface UpdatedPerPatchStore {
  void put(int currentversion, String subject) throws RdfProcessingFailedException;

  Stream<String> ofVersion(int version);

  void close();

  void commit();

  void start();

  boolean isClean();

  void empty();
}
