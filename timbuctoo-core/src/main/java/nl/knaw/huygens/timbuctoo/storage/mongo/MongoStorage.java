package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.Collection;

import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.BasicStorage;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

public class MongoStorage extends MongoStorageBase implements BasicStorage {

  public MongoStorage(DocTypeRegistry registry, Mongo mongo, DB db, String dbName) {
    super(registry, mongo, db, dbName);
  }

  @Override
  public <T extends Entity> T getItem(Class<T> type, String id) {
    return getCollection(type).findOneById(id);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getAllByType(Class<T> type) {
    DBCursor<T> cursor = getCollection(type).find();
    return new MongoDBIterator<T>(cursor);
  }

  @Override
  //TODO do we still want to use versions for SystemEntities?
  public <T extends Entity> MongoChanges<T> getAllRevisions(Class<T> type, String id) {
    return getVersioningCollection(type).findOneById(id);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getByMultipleIds(Class<T> type, Collection<String> ids) {
    DBCursor<T> cursor = getCollection(type).find(DBQuery.in("_id", ids));
    return new MongoDBIterator<T>(cursor);
  }

  // -------------------------------------------------------------------

  @Override
  public <T extends Entity> String addItem(Class<T> type, T item) throws IOException {
    item.setCreation(item.getLastChange());
    if (item.getId() == null) {
      setNextId(type, item);
    }
    getCollection(type).insert(item);
    return item.getId();
  }

  //
  //  ** The code below is temporarily retained because it contains ideas about versioning **
  //
  //  public <T extends Entity> void addItems(Class<T> type, List<T> items) throws IOException {
  //    JacksonDBCollection<T, String> col = getCollection(type);
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
  //      getVersioningCollection(type).insert(changes);
  //    }
  //  }

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

  @Override
  //TODO Is this still the right way to delete a SystemEntity?
  public <T extends Entity> void deleteItem(Class<T> type, String id, Change change) throws IOException {
    // This needs to be updated once mongo-jackson-mapper fixes their wrapper:
    // Update the actual entity first:
    BasicDBObject settings = new BasicDBObject("^deleted", true);
    DBObject newLastChange = MongoUtils.getObjectForDoc(change);
    settings.put("^lastChange", newLastChange);
    BasicDBObject update = new BasicDBObject("$set", settings);
    update.put("$inc", new BasicDBObject("^rev", 1));
    // This returns the previous version of the entity (!)
    // NB: we don't check the rev prop here. This is because deletion will
    // always work; we simply set the delete prop to true.
    getCollection(type).findAndModify(DBQuery.is("_id", id), update);
  }

}
