package nl.knaw.huygens.timbuctoo.v5.datastores.versionstore;

import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DatabaseWriteException;

public interface VersionStore {
  int getVersion();

  void setVersion(int version) throws DatabaseWriteException;

  void close();

  void commit();

  void start();

  boolean isClean();

  void empty();
}
