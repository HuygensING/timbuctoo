package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.berkeleydb;

import nl.knaw.huygens.timbuctoo.v5.datastores.dto.StoreStatusImpl;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;

public abstract class TripleWriterBase implements QuadHandler {

  private final BerkeleyStore store;
  protected final StoreStatusImpl storeStatus;
  private final long newVersion;

  protected TripleWriterBase(BerkeleyStore store, StoreStatusImpl storeStatus, long newVersion) {
    this.store = store;
    this.storeStatus = storeStatus;
    this.newVersion = newVersion;
  }

  @Override
  public void start(long lineCount) throws LogProcessingFailedException {
    store.startTransaction();
    storeStatus.startUpdate(lineCount);
  }

  @Override
  public void onPrefix(long line, String prefix, String uri) {
  }

  @Override
  public void finish() throws LogProcessingFailedException {
    store.commitTransaction();
    storeStatus.finishUpdate(newVersion);
  }

  @Override
  public void cancel() throws LogProcessingFailedException {
    store.commitTransaction();
  }

}
