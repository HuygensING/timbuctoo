package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.BasicStorage;
import nl.knaw.huygens.timbuctoo.storage.StorageConfiguration;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageUtils;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

public class MongoStorage extends MongoStorageBase implements BasicStorage {

  public MongoStorage(DocTypeRegistry registry, StorageConfiguration conf, Mongo m, DB loanedDB) {
    super(registry);
    dbName = conf.getDbName();
    mongo = m;
    db = loanedDB;
    initializeDB(conf);
  }

  private void initializeDB(StorageConfiguration conf) {
    counterCol = JacksonDBCollection.wrap(db.getCollection(COUNTER_COLLECTION_NAME), Counter.class, String.class);
    entityCollections = conf.getEntityTypes();
  }

  @Override
  public <T extends Entity> T getItem(Class<T> type, String id) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    return col.findOneById(id);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getAllByType(Class<T> cls) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, cls);
    return new MongoDBIteratorWrapper<T>(col.find());
  }

  @Override
  public <T extends Entity> MongoChanges<T> getAllRevisions(Class<T> type, String id) {
    return MongoUtils.getVersioningCollection(db, type).findOneById(id);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getByMultipleIds(Class<T> type, Collection<String> ids) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    return new MongoDBIteratorWrapper<T>(col.find(DBQuery.in("_id", ids)));
  }

  @Override
  public <T extends Entity> T findItem(Class<T> type, String key, String value) throws IOException {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    BasicDBObject query = new BasicDBObject(key, value);
    return col.findOne(query);
  }

  @Override
  public <T extends Entity> T findItem(Class<T> type, T example) throws IOException {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    BasicDBObject query = new BasicDBObject();

    Map<String, Object> searchProperties = new MongoObjectMapper().mapObject(type, example);
    Set<String> keys = searchProperties.keySet();

    for (String key : keys) {
      query.put(key, searchProperties.get(key));
    }
    return col.findOne(query);
  }

  // -------------------------------------------------------------------

  @Override
  public <T extends Entity> String addItem(Class<T> type, T item) throws IOException {
    item.setCreation(item.getLastChange());
    if (item.getId() == null) {
      setNextId(type, item);
    }
    MongoUtils.getCollection(db, type).insert(item);
    return item.getId();
  }

  //
  //  ** The code below is temporarily retained because it contains ideas about versioning **
  //
  //  public <T extends Entity> void addItems(Class<T> type, List<T> items) throws IOException {
  //    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
  //    boolean shouldVersion = versionedDocumentTypes.contains(col.getName());
  //
  //    // Create the changes objects for all these entities:
  //    List<MongoChanges<T>> changes = Lists.newArrayListWithCapacity(items.size());
  //    int lastId = 0;
  //    for (T item : items) {
  //      if (item.getId() == null) {
  //        setNextId(type, item);
  //      }
  //      String itemId = item.getId();
  //      lastId = Math.max(lastId, Integer.parseInt(itemId.substring(3), 10));
  //      if (shouldVersion) {
  //        changes.add(new MongoChanges<T>(item.getId(), item));
  //      }
  //    }
  //
  //    // Update the counter object.
  //    DBObject counterQuery = new BasicDBObject("_id", DocTypeRegistry.getCollectionName(type));
  //    Counter counter = counterCol.findOne(counterQuery);
  //    if (counter == null || counter.next <= lastId) {
  //      // Make sure we fail if the counter changes inbetween the findOne above
  //      // and the findAndModify below:
  //      if (counter != null) {
  //        counterQuery.put("next", counter.next);
  //      }
  //      BasicDBObject inc = new BasicDBObject("$set", new BasicDBObject("next", lastId));
  //      counterCol.findAndModify(counterQuery, null, null, false, inc, false, true);
  //    }
  //    // Insert the items:
  //    col.insert(items);
  //    // Insert the changes:
  //    if (shouldVersion) {
  //      MongoUtils.getVersioningCollection(db, type).insert(changes);
  //    }
  //  }

  @Override
  public <T extends Entity> void updateItem(Class<T> type, String id, T item) throws IOException {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);

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

  @Override
  public <T extends Entity> void setPID(Class<T> type, String pid, String id) {
    BasicDBObject query = new BasicDBObject("_id", id);
    BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("^pid", pid));
    MongoUtils.getCollection(db, type).update(query, update);
  }

  @Override
  public <T extends Entity> void deleteItem(Class<T> type, String id, Change change) throws IOException {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    // This needs to be updated once mongo-jackson-mapper fixes their wrapper:
    // Update the actual entity first:
    BasicDBObject settings = new BasicDBObject("^deleted", true);
    DBObject newLastChange = MongoUtils.getObjectForDoc(change);
    settings.put("^lastChange", newLastChange);
    BasicDBObject update = new BasicDBObject("$set", settings);
    update.put("$inc", new BasicDBObject("^rev", 1));
    // This returns the previous version of the entity (!)
    // NB: we don't check the rev prop here. This is because deletion will
    // always work;
    // we simply set the delete prop to true.
    col.findAndModify(DBQuery.is("_id", id), update);
  }

  @Override
  public <T extends Entity> int removeAll(Class<T> type) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    return col.remove(new BasicDBObject()).getN();
  }

  @Override
  public <T extends Entity> int removeByDate(Class<T> type, String dateField, Date dateValue) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    Query query = DBQuery.lessThan(dateField, dateValue);
    return col.remove(query).getN();
  }

  // -------------------------------------------------------------------

  private <T extends Entity> void setNextId(Class<T> type, T item) {
    BasicDBObject idFinder = new BasicDBObject("_id", docTypeRegistry.getINameForType(type));
    BasicDBObject counterIncrement = new BasicDBObject("$inc", new BasicDBObject("next", 1));

    // Find by id, return all fields, use default sort, increment the counter,
    // return the new object, create if no object exists:
    Counter newCounter = counterCol.findAndModify(idFinder, null, null, false, counterIncrement, true, true);

    String newId = StorageUtils.formatEntityId(type, newCounter.next);
    item.setId(newId);
  }

}
