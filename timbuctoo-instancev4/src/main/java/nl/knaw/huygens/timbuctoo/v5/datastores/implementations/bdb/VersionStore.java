package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;

import java.util.stream.Stream;

public class VersionStore {

  private final BdbWrapper<String, Integer> bdbWrapper;

  public VersionStore(BdbWrapper<String, Integer> bdbWrapper)
    throws DataStoreCreationException {

    this.bdbWrapper = bdbWrapper;
  }

  public int getVersion() {
    try (Stream<Integer> values = bdbWrapper.databaseGetter()
      .getAll()
      .getValues()) {
      return values.findAny().orElse(-1);
    }
  }

  public void setVersion(int version) throws DatabaseWriteException {
    bdbWrapper.put("version", version);
  }
}
