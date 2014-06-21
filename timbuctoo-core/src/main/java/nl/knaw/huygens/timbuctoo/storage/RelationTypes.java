package nl.knaw.huygens.timbuctoo.storage;

import java.util.concurrent.ExecutionException;

import nl.knaw.huygens.timbuctoo.model.RelationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Provides for access to stored relation types.
 */
public class RelationTypes {

  private static final Logger LOG = LoggerFactory.getLogger(RelationTypes.class);

  private final Storage storage;
  
  /** Caches relation types by id. */
  private LoadingCache<String, RelationType> idCache;
  
  /** Caches relation types by name. */
  private LoadingCache<String, RelationType> nameCache;

  public RelationTypes(Storage storage) {
    this.storage = storage;
    setupIdCache();
    setupNameCache();
  }

  private void setupIdCache() {
    idCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, RelationType>() {
      @Override
      public RelationType load(String id) throws StorageException {
        RelationType type = storage.getItem(RelationType.class, id);
        if (type == null) {
          // Not allowed to return null
          throw new StorageException("item does not exist");
        }
        return type;
      }
    });
  }

  private void setupNameCache() {
    nameCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, RelationType>() {
      @Override
      public RelationType load(String name) throws StorageException {
        RelationType type = storage.findItemByProperty(RelationType.class, "regularName", name);
        if (type == null) {
          type = storage.findItemByProperty(RelationType.class, "inverseName", name);
        }
        if (type == null) {
          // Not allowed to return null
          throw new StorageException("item does not exist");
        }
        return type;
      }
    });
  }

  public void logCacheStats() {
    LOG.info("RelationType id cache {}", idCache.stats());
    LOG.info("RelationType name cache {}", nameCache.stats());
  }

  /**
   * Returns the relation type with the specified id,
   * or {@code null} if no such relation type exists.
   */
  public RelationType getById(String id) {
    try {
      return idCache.get(id);
    } catch (ExecutionException e) {
      LOG.debug("No relation type with id {}", id);
      return null;
    }
  }

  /**
   * Returns the relation type with the specified name,
   * or {@code null} if no such relation type exists.
   */
  public RelationType getByName(String name) {
    try {
      return nameCache.get(name);
    } catch (ExecutionException e) {
      LOG.debug("No relation type with name {}", name);
      return null;
    }
  }

}
