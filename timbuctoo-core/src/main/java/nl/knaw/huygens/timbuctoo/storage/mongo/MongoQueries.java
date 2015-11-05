package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Creates Mongo queries.
 */
public class MongoQueries {

  //last changed date field
  public static final String MODIFIED_DATE_FIELD = "^modified.timeStamp";

  public DBObject selectAll() {
    return new BasicDBObject();
  }

  public DBObject selectById(String id) {
    return new BasicDBObject("_id", id);
  }

  public DBObject getRevisionFromVersionCollection(String id, int revision) {
    DBObject query = selectById(id);
    query.putAll(getRevisionProjection(revision));

    return query;
  }

  /**
   * Create a query to find an {@code Entity} in a regular collection.
   * @param id the id of the {@code Entity}.
   * @param revision the revision the {@code Entity} should have.
   * @return the query.
   */
  public DBObject selectByIdAndRevision(String id, int revision) {
    DBObject query = new BasicDBObject();
    query.put("_id", id);
    query.put("^rev", revision);
    return query;
  }

  /**
   * Create a projection to find the needed revision.
   * @param revision the revision of the {@code Entity}.
   * @return the projection.
   */
  public DBObject getRevisionProjection(int revision) {
    return new BasicDBObject("versions", new BasicDBObject("$elemMatch", new BasicDBObject("^rev", revision)));
  }

  public DBObject selectByProperty(String propertyName, Object value) {
    return new BasicDBObject(propertyName, value);
  }

  public DBObject selectByProperty(String propertyName, List<String> relationTypeIds) {
    return new BasicDBObject(propertyName, new BasicDBObject("$in", relationTypeIds));
  }

  public DBObject selectByModifiedDate(Date dateValue) {
    return new BasicDBObject(MODIFIED_DATE_FIELD, new BasicDBObject("$lt", dateValue.getTime()));
  }

  public DBObject selectVariation(Class<? extends DomainEntity> type) {
    return new BasicDBObject(DomainEntity.VARIATIONS, TypeNames.getInternalName(type));
  }

  public DBObject selectVariationWithoutPID(Class<? extends DomainEntity> type) {
    return new BasicDBObject(DomainEntity.VARIATIONS, TypeNames.getInternalName(type)) //
        .append(DomainEntity.PID, new BasicDBObject("$exists", false));
  }

  /**
   * Returns a query for selecting non-persistent entities
   * with an id that occurs in the specified id list.
   */
  public DBObject selectNonPersistent(List<String> ids) {
    DBObject query = new BasicDBObject("_id", new BasicDBObject("$in", ids));
    query.put(DomainEntity.PID, null);
    return query;
  }

  /**
   * Returns a query for selecting relations of an entity by using its id.
   * This assumes that entity id's are unique over the various collections.
   */
  public DBObject selectRelationsByEntityId(String id) {
    return or(new BasicDBObject(Relation.SOURCE_ID, id), new BasicDBObject(Relation.TARGET_ID, id));
  }

  private DBObject or(DBObject... subQueries) {
    BasicDBList or = new BasicDBList();
    for (DBObject dbObject : subQueries) {
      or.add(dbObject);
    }

    return new BasicDBObject("$or", or);
  }

  /**
   * Returns a query for selecting relations of multiple entities by using the id's.
   * This assumes that entity id's are unique over the various collections.
   */
  public DBObject selectRelationsByEntityIds(List<String> ids) {
    return or(in(Relation.SOURCE_ID, ids), in(Relation.TARGET_ID, ids));
  }

  private DBObject in(String fieldName, List<String> ids) {
    return new BasicDBObject(fieldName, new BasicDBObject("$in", ids));
  }

  public DBObject selectRelationsByIds(String sourceId, String targetId, String relationTypeId) {
    DBObject query = new BasicDBObject();
    if (sourceId != null) {
      query.put(Relation.SOURCE_ID, sourceId);
    }
    if (targetId != null) {
      query.put(Relation.TARGET_ID, targetId);
    }
    if (relationTypeId != null) {
      query.put(Relation.TYPE_ID, relationTypeId);
    }
    return query;
  }

  public DBObject setPropertiesToValue(Map<String, Object> propertyMap) {
    BasicDBObject set = new BasicDBObject();
    for (Entry<String, Object> entry : propertyMap.entrySet()) {
      set.append(entry.getKey(), entry.getValue());
    }
    return new BasicDBObject("$set", set);
  }

  public DBObject incrementRevision() {
    return new BasicDBObject("$inc", new BasicDBObject("^rev", 1));
  }

}
