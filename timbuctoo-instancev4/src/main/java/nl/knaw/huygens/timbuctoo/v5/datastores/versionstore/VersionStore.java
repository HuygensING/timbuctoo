package nl.knaw.huygens.timbuctoo.v5.datastores.versionstore;

import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;

public interface VersionStore {
  int getVersion();

  void setVersion(int version) throws RdfProcessingFailedException;

  void close();

  void commit();

  void start();

  boolean isClean();

  void empty();
}
