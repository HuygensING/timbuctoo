package nl.knaw.huygens.timbuctoo.v5.datastores;

import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;

import java.util.HashMap;
import java.util.Map;

public class CachedDataStoreFactory<T> implements SingleDataStoreFactory<T> {
  private final SingleDataStoreFactory<T> inner;
  private Map<String, T> cache = new HashMap();

  public CachedDataStoreFactory(SingleDataStoreFactory<T> inner) {
    this.inner = inner;
  }

  @Override
  public T getOrCreate(String userId, String dataSetId) throws DataStoreCreationException {
    synchronized (cache) {
      String key = userId + "_" + dataSetId;
      if (!cache.containsKey(key)) {
        cache.put(key, inner.getOrCreate(userId, dataSetId));
      }
      return cache.get(key);
    }
  }
}
