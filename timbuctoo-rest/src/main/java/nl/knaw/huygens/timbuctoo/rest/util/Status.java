package nl.knaw.huygens.timbuctoo.rest.util;

import nl.knaw.huygens.timbuctoo.index.IndexStatus;
import nl.knaw.huygens.timbuctoo.storage.StorageStatus;

public class Status {

  private StorageStatus storageStatus;
  private IndexStatus indexStatus;

  public StorageStatus getStorageStatus() {
    return storageStatus;
  }

  public void setStorageStatus(StorageStatus status) {
    storageStatus = status;
  }

  public IndexStatus getIndexStatus() {
    return indexStatus;
  }

  public void setIndexStatus(IndexStatus status) {
    indexStatus = status;
  }

}
