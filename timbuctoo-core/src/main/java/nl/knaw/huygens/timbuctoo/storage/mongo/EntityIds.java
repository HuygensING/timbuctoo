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

import java.util.Map;

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;

/**
 * A collection with id's for stored entities.
 * Each primitive entity collection has its series of increasing id's.
 */
@Singleton
public class EntityIds {

  private static final String COLLECTION_NAME = "counters";
  private static final String ID_PROPERTY = "_id";

  private final JacksonDBCollection<Counter, String> counters;
  private final Map<Class<? extends Entity>, String> prefixes = Maps.newHashMap();

  @Inject
  public EntityIds(TypeRegistry registry, MongoDB mongoDB) throws ModelException {
    DBCollection collection = mongoDB.getCollection(COLLECTION_NAME);
    counters = JacksonDBCollection.wrap(collection, Counter.class, String.class);
    registerPrefixes(registry);
  }

  public void registerPrefixes(TypeRegistry registry) throws ModelException {
    for (Class<? extends Entity> type : registry.getSystemEntityTypes()) {
      registerPrefix(type);
    }
    for (Class<? extends Entity> type : registry.getDomainEntityTypes()) {
      if (TypeRegistry.isPrimitiveDomainEntity(type)) {
        registerPrefix(type);
      } else if (type.getAnnotation(IDPrefix.class) != null) {
        throw new ModelException("Illegal IDPrefix annotation for %s", type);
      }
    }
  }

  private void registerPrefix(Class<? extends Entity> type) throws ModelException {
    IDPrefix annotation = type.getAnnotation(IDPrefix.class);
    if (annotation == null) {
      throw new ModelException("Missing IDPrefix annotation for %s", type);
    }
    String prefix = annotation.value();
    if (prefixes.containsValue(prefix)) {
      throw new ModelException("Duplicate IDPrefix annotation value %s for %s", prefix, type);
    }
    prefixes.put(type, prefix);
  }

  public String getNextId(Class<? extends Entity> type) throws StorageException {
    try {
      Class<? extends Entity> baseType = TypeRegistry.getBaseClass(type);
      String id = TypeNames.getInternalName(baseType);
      BasicDBObject query = new BasicDBObject(ID_PROPERTY, id);
      BasicDBObject increment = new BasicDBObject("$inc", new BasicDBObject("next", 1));

      // Find by id, return all fields, use default sort, increment the counter,
      // return the new object, create if no object exists:
      Counter counter = counters.findAndModify(query, null, null, false, increment, true, true);

      return String.format("%s%012d", prefixes.get(baseType), counter.next);
    } catch (MongoException e) {
      throw new StorageException(e);
    }
  }

  private static class Counter {
    @JsonProperty(ID_PROPERTY)
    public String id;
    public long next;
  }

}
