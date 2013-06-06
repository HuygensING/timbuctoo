package nl.knaw.huygens.repository.storage.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.annotations.IDPrefix;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

@Singleton
public class MongoModifiableStorage extends MongoStorageImpl {

  @Inject
  public MongoModifiableStorage(StorageConfiguration conf, DocTypeRegistry docTypeRegistry) throws UnknownHostException, MongoException {
    super(conf, docTypeRegistry);
  }

  public MongoModifiableStorage(StorageConfiguration conf, Mongo m, DB loanedDB, DocTypeRegistry docTypeRegistry) throws UnknownHostException, MongoException {
    super(conf, m, loanedDB, docTypeRegistry);
  }

  // TODO make unit test: add & retrieve
  @Override
  public <T extends Document> void addItem(Class<T> type, T item) throws IOException {
    item.setCreation(item.getLastChange());
    if (item.getId() == null) {
      setNextId(type, item);
    }
    MongoUtils.getCollection(db, type).insert(item);
  }

  @Override
  public <T extends Document> void addItems(Class<T> type, List<T> items) throws IOException {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    boolean shouldVersion = versionedDocumentTypes.contains(col.getName());

    // Create the changes objects for all these documents:
    List<MongoChanges<T>> changes = Lists.newArrayListWithCapacity(items.size());
    int lastId = 0;
    for (T item : items) {
      if (item.getId() == null) {
        setNextId(type, item);
      }
      String itemId = item.getId();
      lastId = Math.max(lastId, Integer.parseInt(itemId.substring(3), 10));
      if (shouldVersion) {
        changes.add(new MongoChanges<T>(item.getId(), item));
      }
    }

    // Update the counter object.
    DBObject counterQuery = new BasicDBObject("_id", DocTypeRegistry.getCollectionName(type));
    Counter counter = counterCol.findOne(counterQuery);
    if (counter == null || counter.next <= lastId) {
      // Make sure we fail if the counter changes inbetween the findOne above
      // and the findAndModify below:
      if (counter != null) {
        counterQuery.put("next", counter.next);
      }
      BasicDBObject inc = new BasicDBObject("$set", new BasicDBObject("next", lastId));
      counterCol.findAndModify(counterQuery, null, null, false, inc, false, true);
    }
    // Insert the items:
    col.insert(items);
    // Insert the changes:
    if (shouldVersion) {
      MongoUtils.getVersioningCollection(db, type).insert(changes);
    }
  }

  @Override
  public <T extends Document> void updateItem(Class<T> type, String id, T item) throws IOException {
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
      throw new IOException("The document was modified since you loaded it!");
    }
  }

  @Override
  public <T extends Document> void setPID(Class<T> type, String pid, String id) {
    BasicDBObject query = new BasicDBObject("_id", id);
    BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("^pid", pid));
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);

    col.update(query, update);
  }

  @Override
  public <T extends Document> void deleteItem(Class<T> type, String id, Change change) throws IOException {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    // This needs to be updated once mongo-jackson-mapper fixes their wrapper:
    // Update the actual document first:
    BasicDBObject settings = new BasicDBObject("^deleted", true);
    DBObject newLastChange = MongoUtils.getObjectForDoc(change);
    settings.put("^lastChange", newLastChange);
    BasicDBObject update = new BasicDBObject("$set", settings);
    update.put("$inc", new BasicDBObject("^rev", 1));
    // This returns the previous version of the document (!)
    // NB: we don't check the rev prop here. This is because deletion will
    // always work;
    // we simply set the delete prop to true.
    col.findAndModify(DBQuery.is("_id", id), update);
  }

  @Override
  public void empty() {
    db.cleanCursors(true);
    mongo.dropDatabase(dbName);
    db = mongo.getDB(dbName);
  }

  public void resetDB(DB db) {
    this.db = db;
  }

  private <T extends Document> void setNextId(Class<T> cls, T item) {
    BasicDBObject idFinder = new BasicDBObject("_id", DocTypeRegistry.getCollectionName(cls));
    BasicDBObject counterIncrement = new BasicDBObject("$inc", new BasicDBObject("next", 1));

    // Find by id, return all fields, use default sort, increment the counter,
    // return the new object, create if no object exists:
    Counter newCounter = counterCol.findAndModify(idFinder, null, null, false, counterIncrement, true, true);

    String newId = cls.getAnnotation(IDPrefix.class).value() + String.format("%1$010d", newCounter.next);
    item.setId(newId);
  }
}
