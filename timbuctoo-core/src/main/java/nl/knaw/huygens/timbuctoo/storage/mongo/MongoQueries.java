package nl.knaw.huygens.timbuctoo.storage.mongo;

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

  public DBObject selectByKeyValue(String key, Object value) {
    return new BasicDBObject(key, value);
  }

}
