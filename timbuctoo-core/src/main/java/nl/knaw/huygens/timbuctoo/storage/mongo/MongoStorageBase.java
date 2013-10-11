package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageUtils;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * Implementation base for Mongo storage classes.
 */
public class MongoStorageBase {

  private static final Logger LOG = LoggerFactory.getLogger(MongoStorageBase.class);
  private static final String COUNTER_COLLECTION_NAME = "counters";

  protected final DocTypeRegistry docTypeRegistry;
  private final Mongo mongo;
  protected DB db;
  private final String dbName;
  protected JacksonDBCollection<Counter, String> counters;

  public MongoStorageBase(DocTypeRegistry registry, Mongo mongo, DB db, String dbName) {
    docTypeRegistry = registry;
    this.mongo = mongo;
    this.db = db;
    this.dbName = dbName;
    counters = JacksonDBCollection.wrap(db.getCollection(COUNTER_COLLECTION_NAME), Counter.class, String.class);
  }

  public void empty() {
    db.cleanCursors(true);
    mongo.dropDatabase(dbName);
    db = mongo.getDB(dbName);
  }

  public void close() {
    db.cleanCursors(true);
    mongo.close();
    LOG.info("Closed");
  }

  public DB getDB() {
    return db;
  }

  public void resetDB(DB db) {
    this.db = db;
  }

  // ---- support ------------------------------------------------------

  protected <T extends Entity> JacksonDBCollection<T, String> getCollection(Class<T> type) {
    return MongoUtils.getCollection(db, type);
  }

  protected <T extends Entity> JacksonDBCollection<MongoChanges<T>, String> getVersioningCollection(Class<T> type) {
    return MongoUtils.getVersioningCollection(db, type);
  }

  /**
   * Sets the id of the specified entity to the next value
   * for the collection in which the entity is stored.
   */
  protected <T extends Entity> void setNextId(Class<T> type, T entity) {
    // This works for both system and domain entities
    Class<? extends Entity> baseType = docTypeRegistry.getBaseClass(type);
    BasicDBObject idFinder = new BasicDBObject("_id", docTypeRegistry.getINameForType(baseType));
    BasicDBObject counterIncrement = new BasicDBObject("$inc", new BasicDBObject("next", 1));

    // Find by id, return all fields, use default sort, increment the counter,
    // return the new object, create if no object exists:
    Counter counter = counters.findAndModify(idFinder, null, null, false, counterIncrement, true, true);

    String id = StorageUtils.formatEntityId(type, counter.next);
    entity.setId(id);
  }

  // --- system entities -----------------------------------------------

  public <T extends SystemEntity> T findItemByKey(Class<T> type, String key, String value) throws IOException {
    BasicDBObject query = new BasicDBObject(key, value);
    return getCollection(type).findOne(query);
  }

  public <T extends SystemEntity> T findItem(Class<T> type, T example) throws IOException {
    Map<String, Object> properties = new MongoObjectMapper().mapObject(type, example);
    BasicDBObject query = new BasicDBObject(properties);
    return getCollection(type).findOne(query);
  }

  public <T extends SystemEntity> int removeAll(Class<T> type) {
    return getCollection(type).remove(new BasicDBObject()).getN();
  }

  public <T extends SystemEntity> int removeByDate(Class<T> type, String dateField, Date dateValue) {
    Query query = DBQuery.lessThan(dateField, dateValue);
    return getCollection(type).remove(query).getN();
  }

  // -------------------------------------------------------------------

  public static class Counter {
    @JsonProperty("_id")
    public String id;
    public long next;
  }

}
