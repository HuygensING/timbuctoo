package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.BasicStorage;
import nl.knaw.huygens.timbuctoo.storage.EmptyStorageIterator;
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

  public MongoStorage(TypeRegistry registry, Mongo mongo, DB db, String dbName) {
    super(registry, mongo, db, dbName);
  }

  @Override
  public <T extends Entity> T getItem(Class<T> type, String id) {
    return getCollection(type).findOneById(id);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getAllByType(Class<T> type) {
    DBCursor<T> cursor = getCollection(type).find();
    return (cursor != null) ? new MongoDBIterator<T>(cursor) : new EmptyStorageIterator<T>();
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

}
