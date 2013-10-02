package nl.knaw.huygens.repository.storage.mongo;

import java.util.Set;

import nl.knaw.huygens.repository.config.DocTypeRegistry;

import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
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

  public static class Counter {
    @JsonProperty("_id")
    public String id;
    public long next;
  }

}
