package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

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

  public EntityIds(MongoDB mongoDB, TypeRegistry registry) {
    typeRegistry = registry;
    DBCollection collection = mongoDB.getCollection(ID_COLLECTION_NAME);
    counters = JacksonDBCollection.wrap(collection, Counter.class, String.class);
    counterIdCache = CacheBuilder.newBuilder().build(new CacheLoader<Class<? extends Entity>, String>() {
      @Override
      public String load(Class<? extends Entity> type) {
        Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
        return TypeNames.getInternalName(baseType);
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

    return formatEntityId(type, counter.next);
  }

  public static final String UNKNOWN_ID_PREFIX = "UNKN";

  /**
   * Returns the prefix of an entity id.
   */
  public static String getIDPrefix(Class<?> type) {
    if (type != null && Entity.class.isAssignableFrom(type)) {
      IDPrefix annotation = type.getAnnotation(IDPrefix.class);
      if (annotation != null) {
        return annotation.value();
      } else {
        return getIDPrefix(type.getSuperclass());
      }
    }
    return UNKNOWN_ID_PREFIX;
  }

  /**
   * Returns a formatted entity id.
   */
  public static String formatEntityId(Class<? extends Entity> type, long counter) {
    return String.format("%s%012d", getIDPrefix(type), counter);
  }

  private static class Counter {
    @JsonProperty("_id")
    public String id;
    public long next;
  }

}
