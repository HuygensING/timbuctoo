package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.datastores.versionstore.VersionStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

class BdbVersionStore implements VersionStore {

  private static final Logger LOG = LoggerFactory.getLogger(BdbVersionStore.class);
  private final BdbWrapper<String, Integer> bdbWrapper;

  BdbVersionStore(BdbWrapper<String, Integer> bdbWrapper)
    throws DataStoreCreationException {

    this.bdbWrapper = bdbWrapper;
  }

  @Override
  public int getVersion() {
    try (Stream<Integer> values = bdbWrapper.databaseGetter()
      .getAll()
      .getValues()) {
      return values.findAny().orElse(-1);
    }
  }

  @Override
  public void setVersion(int version) throws DatabaseWriteException {
    bdbWrapper.put("version", version);
  }

  @Override
  public void close() {
    try {
      bdbWrapper.close();
    } catch (Exception e) {
      LOG.error("Exception closing VersionStore", e);
    }
  }

  @Override
  public void commit() {
    bdbWrapper.commit();
  }

  @Override
  public void start() {
    bdbWrapper.beginTransaction();
  }

  @Override
  public boolean isClean() {
    return bdbWrapper.isClean();
  }

  @Override
  public void empty() {
    bdbWrapper.empty();
  }
}
