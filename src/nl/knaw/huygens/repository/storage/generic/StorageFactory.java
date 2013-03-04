package nl.knaw.huygens.repository.storage.generic;


import java.lang.reflect.Constructor;
import java.util.Map;

import com.google.common.collect.Maps;

import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.storage.Storage;



public class StorageFactory {
  private static Map<String, Storage> storages = Maps.newHashMap();

  public static Storage getInstance(StorageConfiguration conf, DocumentTypeRegister docTypeRegistry) {
    StorageType storageType = conf.getType();
    return get(storageType, conf, docTypeRegistry);
  }
  

  private static Storage get(StorageType type, StorageConfiguration storageConfiguration, DocumentTypeRegister docTypeRegistry) {
    try {
      String k = storageConfiguration.getKey();
      if (!storages.containsKey(k)) {
        Class<? extends Storage> cls = type.getCls();
        Constructor<? extends Storage> constructor;
        constructor = cls.getConstructor(StorageConfiguration.class, DocumentTypeRegister.class);
        Storage storage = constructor.newInstance(storageConfiguration, docTypeRegistry);
        storages.put(k, storage);
      }
      return storages.get(k);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new RuntimeException("Couldn't create storage.");
    }
  }
}
