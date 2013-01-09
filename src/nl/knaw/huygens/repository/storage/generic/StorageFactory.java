package nl.knaw.huygens.repository.storage.generic;


import java.lang.reflect.Constructor;
import java.util.Map;

import com.google.common.collect.Maps;

import nl.knaw.huygens.repository.storage.Storage;



public class StorageFactory {
  private static Map<String, Storage> storages = Maps.newHashMap();

  public static Storage getInstance(StorageConfiguration conf) {
    StorageType storageType = conf.getType();
    return get(storageType, conf);
  }
  

  private static Storage get(StorageType type, StorageConfiguration storageConfiguration) {
    try {
      String k = storageConfiguration.getKey();
      if (!storages.containsKey(k)) {
        Class<? extends Storage> cls = type.getCls();
        Constructor<? extends Storage> constructor;
        constructor = cls.getConstructor(StorageConfiguration.class);
        Storage storage = constructor.newInstance(storageConfiguration);
        storages.put(k, storage);
      }
      return storages.get(k);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new RuntimeException("Couldn't create storage.");
    }
  }
}
