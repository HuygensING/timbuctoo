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
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.annotation.JsonProperty;
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

  public static final String ID_COLLECTION_NAME = "counters";
  public static final String UNKNOWN_ID_PREFIX = "UNKN";

  private final JacksonDBCollection<Counter, String> counters;

  @Inject
  public EntityIds(MongoDB mongoDB) {
    DBCollection collection = mongoDB.getCollection(ID_COLLECTION_NAME);
    counters = JacksonDBCollection.wrap(collection, Counter.class, String.class);
  }

  public String getNextId(Class<? extends Entity> type) throws StorageException {
    try {
      Class<? extends Entity> baseType = TypeRegistry.getBaseClass(type);
      String counterId = TypeNames.getInternalName(baseType);
      BasicDBObject query = new BasicDBObject("_id", counterId);
      BasicDBObject increment = new BasicDBObject("$inc", new BasicDBObject("next", 1));

      // Find by id, return all fields, use default sort, increment the counter,
      // return the new object, create if no object exists:
      Counter counter = counters.findAndModify(query, null, null, false, increment, true, true);

      return String.format("%s%012d", getIDPrefix(baseType), counter.next);
    } catch (MongoException e) {
      throw new StorageException(e);
    }
  }

  /**
   * Returns the prefix of an entity id.
   */
  private String getIDPrefix(Class<? extends Entity> type) {
    IDPrefix annotation = type.getAnnotation(IDPrefix.class);
    return (annotation != null) ? annotation.value() : UNKNOWN_ID_PREFIX;
  }

  private static class Counter {
    @JsonProperty("_id")
    public String id;
    public long next;
  }

}
