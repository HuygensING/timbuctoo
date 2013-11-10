package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.util.Date;
import java.util.Map;

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

  public DBObject selectByProperty(String key, Object value) {
    return new BasicDBObject(key, value);
  }

  public DBObject selectByProperties(Map<String, Object> properties) {
    return new BasicDBObject(properties);
  }

  public DBObject selectByDate(String dateField, Date dateValue) {
    return DBQuery.lessThan(dateField, dateValue);
  }

}
