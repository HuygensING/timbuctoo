package nl.knaw.huygens.repository.storage.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.model.util.IDPrefix;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;

@Singleton
public class MongoModifiableStorage extends MongoStorage implements Storage {

  @Inject
  public MongoModifiableStorage(StorageConfiguration conf, DocumentTypeRegister docTypeRegistry) throws UnknownHostException, MongoException {
    super(conf, docTypeRegistry);
  }

  public MongoModifiableStorage(StorageConfiguration conf, Mongo m, DB loanedDB, DocumentTypeRegister docTypeRegistry) throws UnknownHostException,
      MongoException {
    super(conf, m, loanedDB, docTypeRegistry);
  }

  @Override
  public <T extends Document> void addItem(T newItem, Class<T> cls) throws IOException {
    if (newItem.getId() == null) {
      setNextId(cls, newItem);
    }
    addVersion(newItem.getId(), newItem, cls, true);
  }

  @Override
  public <T extends Document> void addItems(List<T> items, Class<T> cls) throws IOException {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, cls);
    boolean shouldVersion = versionedDocumentTypes.contains(col.getName());

    // Create the changes objects for all these documents:
    List<MongoChanges<T>> changes = Lists.newArrayListWithCapacity(items.size());
    int lastId = 0;
    for (T item : items) {
      String itemId = item.getId();
      lastId = Math.max(lastId, Integer.parseInt(itemId.substring(3), 10));
      if (shouldVersion) {
        changes.add(new MongoChanges<T>(item.getId(), item));
      }
    }

    // Update the counter object.
    DBObject counterQuery = new BasicDBObject("_id", DocumentTypeRegister.getCollectionName(cls));
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
      MongoUtils.getVersioningCollection(db, cls).insert(changes);
    }
  }

  @Override
  public <T extends Document> void updateItem(String id, T updatedItem, Class<T> cls) throws IOException {
    addVersion(id, updatedItem, cls);
    updatedItem.setRev(updatedItem.getRev() + 1);
  }

  @Override
  public <T extends Document> void setPID(Class<T> cls, String pid, String id) {
    BasicDBObject query = new BasicDBObject("_id", id);
    BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("^pid", pid));
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, cls);

    col.update(query, update);
  }

  @Override
  public <T extends Document> void deleteItem(String id, Class<T> cls, Change change) throws IOException {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, cls);
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
    T item = col.findAndModify(DBQuery.is("_id", id), update);

    // Then update the versioning table:
    JacksonDBCollection<MongoChanges<T>, String> versionCol = MongoUtils.getVersioningCollection(db, cls);
    int oldRev = item.getRev();
    item.setRev(oldRev + 1);
    item.setLastChange(change);
    item.setDeleted(true);
    changeVersionObj(id, oldRev, versionCol, item);
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
    BasicDBObject idFinder = new BasicDBObject("_id", DocumentTypeRegister.getCollectionName(cls));
    BasicDBObject counterIncrement = new BasicDBObject("$inc", new BasicDBObject("next", 1));

    // Find by id, return all fields, use default sort, increment the counter,
    // return the new object, create if no object exists:
    Counter newCounter = counterCol.findAndModify(idFinder, null, null, false, counterIncrement, true, true);

    String newId = cls.getAnnotation(IDPrefix.class).value() + String.format("%1$010d", newCounter.next);
    item.setId(newId);
  }

  private <T extends Document> void addVersion(String id, T item, Class<T> cls) throws IOException {
    addVersion(id, item, cls, false);
  }

  private <T extends Document> void addVersion(String id, T item, Class<T> cls, boolean doInsert) throws IOException {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, cls);
    boolean shouldVersion = versionedDocumentTypes.contains(col.getName());

    int oldRev = item.getRev();
    item.setRev(oldRev + 1);
    if (doInsert) {
      item.setCreation(item.getLastChange());
      col.insert(item);
      if (shouldVersion) {
        updateVersionCol(id, null, item, oldRev, cls);
      }
    } else {
      // This is really evil, but it's very annoying to update only the fields
      // you want
      // (you can't ignore fields, only give an explicit list of everything you
      // want)
      T oldItemWithCreation = col.findOneById(id, new BasicDBObject("^creation", true));
      if (oldItemWithCreation != null) {
        item.setCreation(oldItemWithCreation.getCreation());
      }
      T oldItem = col.findAndModify(DBQuery.is("_id", id).is("^rev", oldRev), MongoUtils.getObjectForDoc(item));
      if (oldItem == null) {
        throw new IOException("The document was modified since you loaded it!");
      }
      if (shouldVersion) {
        updateVersionCol(id, oldItem, item, oldRev, cls);
      }
    }
  }

  private <T extends Document> void updateVersionCol(String id, T oldItem, T item, int oldRev, Class<T> cls) throws MongoException, IOException {
    JacksonDBCollection<MongoChanges<T>, String> versionCol = MongoUtils.getVersioningCollection(db, cls);
    long count = versionCol.count(DBQuery.is("_id", id));
    if (count != 0 && oldItem != null) {
      changeVersionObj(id, oldRev, versionCol, item);
    } else {
      versionCol.insert(new MongoChanges<T>(id, item));
    }
  }

  private <T extends Document> void changeVersionObj(String id, int oldRev, JacksonDBCollection<MongoChanges<T>, String> versionCol, T item)
      throws MongoException {
    MongoChanges<T> oldItem = versionCol.findOne(DBQuery.is("_id", id));
    oldItem.getRevisions().add(item);
    WriteResult<MongoChanges<T>, String> updateResult = versionCol.updateById(id, oldItem);

    CommandResult cachedLastError = updateResult.getCachedLastError();
    if (!cachedLastError.ok() || updateResult.getN() != 1) {
      throw new MongoException("Updating the version table failed!\n" + cachedLastError.getErrorMessage());
    }
  }

}
