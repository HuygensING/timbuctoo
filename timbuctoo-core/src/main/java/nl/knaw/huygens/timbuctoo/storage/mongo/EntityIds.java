package nl.knaw.huygens.timbuctoo.storage.mongo;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.StorageUtils;

import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;

/**
 * A collection with id's for stored entities.
 * Each primitive entity collection has its series of increasing id's.
 */
public class EntityIds {

  private static final String ID_COLLECTION_NAME = "counters";

  private final TypeRegistry typeRegistry;
  // The counters are stored in a collection, of course
  private final JacksonDBCollection<Counter, String> counters;
  // A cache to avoid repeated inspection of entity classes
  private final LoadingCache<Class<? extends Entity>, String> counterIdCache;

  public EntityIds(DB db, TypeRegistry registry) {
    typeRegistry = registry;
    counters = JacksonDBCollection.wrap(db.getCollection(ID_COLLECTION_NAME), Counter.class, String.class);
    counterIdCache = CacheBuilder.newBuilder().build(new CacheLoader<Class<? extends Entity>, String>() {
      @Override
      public String load(Class<? extends Entity> type) {
        Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
        return typeRegistry.getINameForType(baseType);
      }
    });
  }

  public String getNextId(Class<? extends Entity> type) {
    String counterId = counterIdCache.getUnchecked(type);
    BasicDBObject query = new BasicDBObject("_id", counterId);
    BasicDBObject increment = new BasicDBObject("$inc", new BasicDBObject("next", 1));

    // Find by id, return all fields, use default sort, increment the counter,
    // return the new object, create if no object exists:
    Counter counter = counters.findAndModify(query, null, null, false, increment, true, true);

    return StorageUtils.formatEntityId(type, counter.next);
  }

  private static class Counter {
    @JsonProperty("_id")
    public String id;
    public long next;
  }

}
