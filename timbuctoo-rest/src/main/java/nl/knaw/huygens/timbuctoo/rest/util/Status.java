package nl.knaw.huygens.timbuctoo.rest.util;

import nl.knaw.huygens.timbuctoo.storage.StorageStatus;

public class Status {

  private StorageStatus storageStatus;

  public StorageStatus getStorageStatus() {
    return storageStatus;
  }

  public void setStorageStatus(StorageStatus status) {
    storageStatus = status;
  }

}
