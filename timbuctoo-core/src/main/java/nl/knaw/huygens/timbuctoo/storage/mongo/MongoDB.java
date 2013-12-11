package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * Encapsulates the Mongo database.
 */
public class MongoDB {

  private static final Logger LOG = LoggerFactory.getLogger(MongoDB.class);

  private final Mongo mongo;
  private final DB db;

  public MongoDB(Mongo mongo, DB db) {
    this.mongo = mongo;
    this.db = db;
  }

  public void close() {
    db.cleanCursors(true);
    mongo.close();
    LOG.info("Closed");
  }

  /**
   * Gets a collection with the specified name.
   * If the collection does not exist, a new one is created.
   */
  public DBCollection getCollection(String name) {
    return db.getCollection(name);
  }

  /**
   * Inserts a document into the database.
   */
  public void insert(DBCollection collection, String id, DBObject document) throws IOException {
    collection.insert(document);
    if (collection.find(new BasicDBObject("_id", id)) == null) {
      LOG.error("Failed to insert ({}, {})", collection.getName(), id);
      throw new IOException("Insert failed");
    }
  }

}
