package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.util.Date;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

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

  protected static final String COUNTER_COLLECTION_NAME = "counters";

  protected final DocTypeRegistry docTypeRegistry;

  protected Mongo mongo;
  protected String dbName;
  protected DB db;

  protected JacksonDBCollection<Counter, String> counterCol;
  protected Set<String> entityCollections;

  public MongoStorageBase(DocTypeRegistry registry) {
    docTypeRegistry = registry;
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

  // -------------------------------------------------------------------

  public <T extends SystemEntity> int removeAll(Class<T> type) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    return col.remove(new BasicDBObject()).getN();
  }

  public <T extends SystemEntity> int removeByDate(Class<T> type, String dateField, Date dateValue) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    Query query = DBQuery.lessThan(dateField, dateValue);
    return col.remove(query).getN();
  }

  // -------------------------------------------------------------------

  public static class Counter {
    @JsonProperty("_id")
    public String id;
    public long next;
  }

}
