package nl.knaw.huygens.timbuctoo.storage.mongo;

import static nl.knaw.huygens.timbuctoo.storage.FieldMapper.propertyName;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
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

  public DBObject selectByIdAndRevision(String id, int revision) {
    DBObject query = new BasicDBObject();
    query.put("_id", id);
    query.put("^rev", revision);
    return query;
  }

  public DBObject selectByProperty(String key, Object value) {
    return new BasicDBObject(key, value);
  }

  public DBObject selectByProperty(Class<?> type, String field, Object value) {
    return new BasicDBObject(propertyName(type, field), value);
  }

  /**
   * Generates a query based on the non-null values of an entity.
   */
  public <T extends Entity> DBObject selectByProperties(Class<? super T> type, T entity) {
    Map<String, Field> fieldMap = new FieldMapper().getCompositeFieldMap(type, type, type);
    PropertyMap properties = new PropertyMap(fieldMap, entity);
    return new BasicDBObject(properties);
  }

  public DBObject selectByDate(String dateField, Date dateValue) {
    return DBQuery.lessThan(dateField, dateValue);
  }

  public DBObject selectRelation(Relation relation) {
    DBObject query = new BasicDBObject();
    query.put("^typeId", relation.getTypeId());
    query.put("^sourceId", relation.getSourceId());
    query.put("^targetId", relation.getTargetId());
    return query;
  }

  public DBObject selectVariation(String name) {
    return new BasicDBObject(DomainEntity.VARIATIONS, name);
  }

  public DBObject setProperty(String key, Object value) {
    return new BasicDBObject("$set", new BasicDBObject(key, value));
  }

}
