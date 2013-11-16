package nl.knaw.huygens.timbuctoo.storage.mongo;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.BasicStorage;
import nl.knaw.huygens.timbuctoo.storage.EmptyStorageIterator;
import nl.knaw.huygens.timbuctoo.storage.JsonViews;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

/**
 * Implementation base for Mongo storage classes.
 */
public abstract class MongoStorageBase implements BasicStorage {

  private static final Logger LOG = LoggerFactory.getLogger(MongoStorageBase.class);

  protected final TypeRegistry typeRegistry;

  private final Mongo mongo;
  protected DB db;
  private final String dbName;
  private EntityIds entityIds;

  protected final MongoObjectMapper mongoMapper;
  protected final MongoQueries queries;

  protected final ObjectMapper objectMapper;
  protected final TreeEncoderFactory treeEncoderFactory;
  protected final TreeDecoderFactory treeDecoderFactory;
  protected final VariationInducer inducer;
  protected final VariationReducer reducer;

  public MongoStorageBase(TypeRegistry registry, Mongo mongo, DB db, String dbName) {
    typeRegistry = registry;
    this.mongo = mongo;
    this.db = db;
    this.dbName = dbName;

    entityIds = new EntityIds(db, typeRegistry);
    queries = new MongoQueries();
    mongoMapper = new MongoObjectMapper();
    objectMapper = new ObjectMapper();
    treeEncoderFactory = new TreeEncoderFactory(objectMapper);
    treeDecoderFactory = new TreeDecoderFactory();
    inducer = new VariationInducer(registry);
    reducer = new VariationReducer(registry);
  }

  public void empty() {
    db.cleanCursors(true);
    mongo.dropDatabase(dbName);
    db = mongo.getDB(dbName);
    entityIds = new EntityIds(db, typeRegistry);
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

  public void setEntityIds(EntityIds entityIds) {
    this.entityIds = entityIds;
  }

  // --- entities ------------------------------------------------------

  protected <T extends Entity> T getItem(Class<T> type, DBObject query) throws IOException {
    DBObject item = getDBCollection(type).findOne(query);
    return reducer.reduceDBObject(type, item);
  }

  protected <T extends Entity> StorageIterator<T> getItems(Class<T> type, DBObject query) {
    DBCursor cursor = getDBCollection(type).find(query);
    return (cursor != null) ? new MongoDBVariationIterator<T>(type, cursor, reducer) : new EmptyStorageIterator<T>();
  }

  public <T extends Entity> long count(Class<T> type) {
    Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
    return getCollection(baseType).count();
  }

  // -------------------------------------------------------------------

  @Override
  public <T extends Entity> T getItem(Class<T> type, String id) throws IOException {
    return getCollection(type).findOneById(id);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getAllByType(Class<T> type) {
    org.mongojack.DBCursor<T> cursor = getCollection(type).find();
    return (cursor != null) ? new MongoDBIterator<T>(cursor) : new EmptyStorageIterator<T>();
  }

  @Override
  public <T extends Entity> String addItem(Class<T> type, T item) throws IOException {
    item.setCreation(item.getLastChange());
    if (item.getId() == null) {
      setNextId(type, item);
    }
    getCollection(type).insert(item);
    return item.getId();
  }

  @Override
  public <T extends Entity> void updateItem(Class<T> type, String id, T item) throws IOException {
    JacksonDBCollection<T, String> col = getCollection(type);

    int oldRev = item.getRev();
    item.setRev(oldRev + 1);

    BasicDBObject query = new BasicDBObject("_id", id);
    query.put("^rev", oldRev);

    T oldItemWithCreation = col.findOne(query);
    if (oldItemWithCreation != null) {
      item.setCreation(oldItemWithCreation.getCreation());
    }

    String objectString = new ObjectMapper().writeValueAsString(item);

    DBObject newItem = (DBObject) JSON.parse(objectString);

    T oldItem = col.findAndModify(DBQuery.is("_id", id).is("^rev", oldRev), newItem);
    if (oldItem == null) {
      throw new IOException("The entity was modified since you loaded it!");
    }
  }

  // --- system entities -----------------------------------------------

  @Override
  public <T extends SystemEntity> T findItemByKey(Class<T> type, String key, String value) throws IOException {
    DBObject query = queries.selectByProperty(key, value);
    return getCollection(type).findOne(query);
  }

  @Override
  public <T extends SystemEntity> T findItem(Class<T> type, T example) throws IOException {
    Map<String, Object> properties = mongoMapper.mapObject(type, example);
    DBObject query = queries.selectByProperties(properties);
    return getCollection(type).findOne(query);
  }

  @Override
  public <T extends SystemEntity> void removeItem(Class<T> type, String id) {
    DBObject query = queries.selectById(id);
    getCollection(type).remove(query);
  }

  @Override
  public <T extends SystemEntity> int removeAll(Class<T> type) {
    DBObject query = queries.selectAll();
    return getCollection(type).remove(query).getN();
  }

  @Override
  public <T extends SystemEntity> int removeByDate(Class<T> type, String dateField, Date dateValue) {
    DBObject query = queries.selectByDate(dateField, dateValue);
    return getCollection(type).remove(query).getN();
  }

  // TODO decide whether this needs to be cacheable
  protected RelationType getRelationType(String id) {
    DBObject query = queries.selectById(id);
    return getCollection(RelationType.class).findOne(query);
  }

  // --- domain entities -----------------------------------------------

  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) {
    DBObject query = queries.selectById(id);
    DBObject update = queries.setProperty(DomainEntity.PID, pid);
    getDBCollection(type).update(query, update);
  }

  // --- support -------------------------------------------------------

  private final Map<Class<? extends Entity>, DBCollection> collectionCache = Maps.newHashMap();

  protected <T extends Entity> DBCollection getDBCollection(Class<T> type) {
    DBCollection collection = collectionCache.get(type);
    if (collection == null) {
      Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
      String collectionName = typeRegistry.getINameForType(baseType);
      checkState(collectionName != null, "Unregistered type %s", type.getSimpleName());
      collection = db.getCollection(collectionName);
      collection.setDBDecoderFactory(treeDecoderFactory);
      collection.setDBEncoderFactory(treeEncoderFactory);
      collectionCache.put(type, collection);
      LOG.info("Added {} to collection cache", type.getSimpleName());
    }
    return collection;
  }

  protected <T extends Entity> JacksonDBCollection<T, String> getCollection(Class<T> type) {
    DBCollection col = db.getCollection(getCollectionName(type));
    return JacksonDBCollection.wrap(col, type, String.class, JsonViews.DBView.class);
  }

  protected String getCollectionName(Class<? extends Entity> type) {
    return type.getSimpleName().toLowerCase();
  }

  protected String getVersioningCollectionName(Class<? extends Entity> type) {
    return getCollectionName(type) + "_versions";
  }

  /**
   * Sets the id of the specified entity to the next value
   * for the collection in which the entity is stored.
   */
  protected <T extends Entity> void setNextId(Class<T> type, T entity) {
    entity.setId(entityIds.getNextId(type));
  }

}
