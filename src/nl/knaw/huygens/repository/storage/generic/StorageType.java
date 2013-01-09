package nl.knaw.huygens.repository.storage.generic;

import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.mongo.MongoDBModifiableStorage;

public enum StorageType {
  MONGO(MongoDBModifiableStorage.class);
  private Class<? extends Storage> cls;
  StorageType(Class<? extends Storage> cls) {
    this.cls = cls;
  }
  
  public Class<? extends Storage> getCls() {
    return cls;
  }
}