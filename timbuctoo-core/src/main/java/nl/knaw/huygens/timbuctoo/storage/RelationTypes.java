package nl.knaw.huygens.timbuctoo.storage;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huygens.timbuctoo.model.RelationType;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

/**
 * Provides for access to stored relation types.
 */
public class RelationTypes {

  private static final Logger LOG = LoggerFactory.getLogger(RelationTypes.class);

  private final Storage storage;
  
  /** Caches relation types by id. */
  private LoadingCache<String, RelationType> idCache;

  public RelationTypes(Storage storage) {
    this.storage = storage;
    setupIdCache();
  }

  private void setupIdCache() {
    idCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, RelationType>() {
      @Override
      public RelationType load(String id) throws StorageException {
        // Not allowed to return null
        RelationType type = storage.getItem(RelationType.class, id);
        if (type == null) {
          throw new StorageException("item does not exist");
        }
        return type;
      }
    });
  }

  public void logCacheStats() {
    if (idCache != null) {
      LOG.info("RelationType id cache {}", idCache.stats());
    }
  }

  /**
   * Returns the relation type with the specified id,
   * or {@code null} if no such relation type exists.
   */
  public RelationType getRelationTypeById(String id) {
    try {
      return idCache.get(id);
    } catch (ExecutionException e) {
      LOG.debug("No relation type with id {}: {}", id, e.getMessage());
      return null;
    }
  }

  /*
   * Returns a map for retrieving relation types by their regular name.
   */
  public Map<String, RelationType> getRelationTypeMap() {
    Map<String, RelationType> map = Maps.newHashMap();
    try {
      for (RelationType type : storage.getSystemEntities(RelationType.class).getAll()) {
        map.put(type.getRegularName(), type);
      }
    } catch (StorageException e)	{
      // TODO handle
    }
    return map;
  }

}
