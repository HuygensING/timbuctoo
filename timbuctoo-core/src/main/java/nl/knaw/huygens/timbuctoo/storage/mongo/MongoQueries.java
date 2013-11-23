package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.util.Date;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Relation;

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

  public DBObject selectByProperties(Map<String, Object> properties) {
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

  // FIXME This is correct for the old reducer/inducer, but now it is wrong!
  // Still, it is better to define it in one place...
  public DBObject selectVariation(String name) {
    return new BasicDBObject(name, new BasicDBObject("$ne", null));
  }

  public DBObject setProperty(String key, Object value) {
    return new BasicDBObject("$set", new BasicDBObject(key, value));
  }

}
