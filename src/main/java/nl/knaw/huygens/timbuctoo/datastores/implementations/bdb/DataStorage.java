package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.DatabaseWriteException;

public interface DataStorage {
  String getValue();

  void setValue(String newValue) throws DatabaseWriteException;

  void close() throws Exception;

  void commit();

  void beginTransaction();

  boolean isClean();

  void empty();
}
