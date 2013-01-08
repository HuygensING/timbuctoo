package nl.knaw.huygens.repository.storage.mongo;

import java.util.Map;

import nl.knaw.huygens.repository.model.storage.Storage;

import com.google.common.collect.Maps;


public class StorageFactory {
  protected static enum StorageType {
    MONGO(MongoDBStorage.class);
    private Class<? extends Storage> cls;
    StorageType(Class<? extends Storage> cls) {
      this.cls = cls;
    }

    public Storage get(StorageConfiguration storageConfiguration) {
      try {
        String k = storageConfiguration.getKey();
        if (!storages.containsKey(k)) {
          storages.put(k, cls.getConstructor(StorageConfiguration.class).newInstance(storageConfiguration));
        }
        return storages.get(k);
      } catch (Exception ex) {
        ex.printStackTrace();
        throw new RuntimeException("Couldn't create storage.");
      }
    }
    private static Map<String, Storage> storages = Maps.newHashMap();
  }
  public static Storage getInstance(StorageConfiguration conf) {
    StorageType storageType = conf.getType();
    return storageType.get(conf);
  }
}
