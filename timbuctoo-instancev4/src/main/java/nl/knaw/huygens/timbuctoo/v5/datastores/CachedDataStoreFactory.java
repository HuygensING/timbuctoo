package nl.knaw.huygens.timbuctoo.v5.datastores;

import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;

import java.util.HashMap;
import java.util.Map;

public abstract class CachedDataStoreFactory<T> {
  private Map<String, T> cache = new HashMap<>();

  protected abstract T create(String userId, String dataSetId) throws DataStoreCreationException;

  public T getOrCreate(String userId, String dataSetId) throws DataStoreCreationException {
    synchronized (cache) {
      String key = userId + "_" + dataSetId;
      if (!cache.containsKey(key)) {
        cache.put(key, create(userId, dataSetId));
      }
      return cache.get(key);
    }
  }
}
