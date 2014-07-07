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

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.FieldMapper;
import nl.knaw.huygens.timbuctoo.storage.PropertyMap;

import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Creates Mongo queries.
 */
public class MongoQueries {

  public DBObject selectAll() {
    return new BasicDBObject();
  }

  public DBObject selectById(String id) {
    return new BasicDBObject("_id", id);
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

  public DBObject selectByProperty(Class<?> type, String field, Object value) {
    return new BasicDBObject(propertyName(type, field), value);
  }

  public DBObject selectByProperty(Class<?> type, String field, List<String> relationTypeIds) {
    return new BasicDBObject(propertyName(type, field), new BasicDBObject("$in", relationTypeIds));
  }

  /**
   * Generates a query based on the non-null values of an entity.
   */
  public <T extends Entity> DBObject selectByProperties(Class<? super T> type, T entity) {
    Map<String, Field> fieldMap = new FieldMapper().getCompositeFieldMap(type, type, type);
    PropertyMap properties = new PropertyMap(entity, fieldMap);
    return new BasicDBObject(properties);
  }

  public DBObject selectByDate(Class<?> type, String dateField, Date dateValue) {
    String key = propertyName(type, dateField);
    return new BasicDBObject(key, new BasicDBObject("$lt", dateValue.getTime()));
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
    return DBQuery.or(DBQuery.is("^sourceId", id), DBQuery.is("^targetId", id));
  }

  /**
   * Returns a query for selecting relations of multiple entities by using the id's.
   * This assumes that entity id's are unique over the various collections.
   */
  public DBObject selectRelationsByEntityIds(List<String> ids) {
    return DBQuery.or(DBQuery.in("^sourceId", ids), DBQuery.in("^targetId", ids));
  }

  public DBObject selectRelationsByIds(String sourceId, String targetId, String relationTypeId) {
    DBObject query = new BasicDBObject();
    if (sourceId != null) {
      query.put("^sourceId", sourceId);
    }
    if (targetId != null) {
      query.put("^targetId", targetId);
    }
    if (relationTypeId != null) {
      query.put("^typeId", relationTypeId);
    }
    return query;
  }

}
