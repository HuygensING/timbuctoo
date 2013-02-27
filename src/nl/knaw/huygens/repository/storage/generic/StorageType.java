package nl.knaw.huygens.repository.storage.generic;

import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.mongo.MongoModifiableStorage;
import nl.knaw.huygens.repository.storage.mongo.variation.MongoComplexStorage;
import nl.knaw.huygens.repository.storage.mongo.variation.MongoModifiableVariationStorage;

public enum StorageType {
  MONGO(MongoModifiableStorage.class),
  MONGOVARIATION(MongoModifiableVariationStorage.class),
  MONGOCOMBO(MongoComplexStorage.class);
  
  private Class<? extends Storage> cls;
  StorageType(Class<? extends Storage> cls) {
    this.cls = cls;
  }
  
  public Class<? extends Storage> getCls() {
    return cls;
  }
}