package nl.knaw.huygens.repository.storage.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.bson.BSONObject;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.DBQuery.Query;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.model.util.IDPrefix;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;

public class MongoDBModifiableStorage extends MongoDBStorage implements Storage {

  public MongoDBModifiableStorage(StorageConfiguration conf) throws UnknownHostException, MongoException {
    super(conf);
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
    List<MongoChanges> changes = Lists.newArrayListWithCapacity(items.size());
    int lastId = 0;
    for (T item : items) {
      String itemId = item.getId();
      lastId = Math.max(lastId, Integer.parseInt(itemId.substring(3), 10));
      if (shouldVersion) {
        changes.add(new MongoChanges(item.getId(), MongoUtils.getObjectForDoc(item)));
      }
    }

    // Update the counter object.
    DBObject counterQuery = new BasicDBObject("_id", MongoUtils.getCollectionName(cls));
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
    JacksonDBCollection<MongoChanges, String> versionCol = MongoUtils.getVersioningCollection(db, cls);
    int oldRev = item.getRev();
    // Use item.isDeleted() rather than a hardcoded 'false',
    // because we're technically not sure if it wasn't deleted already.
    BasicDBObject fromnext = new BasicDBObject("^deleted", item.isDeleted());
    BasicDBObject frompast = new BasicDBObject("^deleted", true);
    frompast.put("^rev", oldRev + 1);
    frompast.put("^lastChange", newLastChange);
    fromnext.put("^rev", oldRev);
    fromnext.put("^lastChange", MongoUtils.getObjectForDoc(item.getLastChange()));
    changeVersionObj(id, fromnext, frompast, oldRev, versionCol);
    item.setRev(oldRev + 1);
  }

  @Override
  public void empty() {
    db.cleanCursors(true);
    mongo.dropDatabase(dbName);
    db = mongo.getDB(dbName);
  }

  @Override
  public <T extends Document> void removeReference(Class<T> cls, List<String> accessorList, List<String> referringIds, String referredId, Change change) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, cls);
    JacksonDBCollection<MongoChanges, String> versionCol = MongoUtils.getVersioningCollection(db, cls);
    List<T> docs = col.find(DBQuery.in("_id", referringIds)).toArray();
    for (T doc : docs) {
      doc.setLastChange(change);
      DBObject newObj;
      DBObject origObj;
      try {
        origObj = MongoUtils.getObjectForDoc(doc);
        newObj = MongoUtils.getObjectForDoc(doc);
      } catch (IOException ex) {
        continue;
      }
      removeNested(newObj, accessorList, referredId);
      col.update(DBQuery.is("_id", doc.getId()).is("^rev", doc.getRev()), newObj);
      changeVersionObj(doc.getId(), MongoDiff.diffToNewObject(newObj, origObj), MongoDiff.diffToNewObject(origObj, newObj), doc.getRev(), versionCol);
    }
  }

  private boolean removeNested(Object newObj, List<String> accessorList, String referredId) {
    // Deal with lists first:
    if (newObj instanceof List) {
      @SuppressWarnings("unchecked")
      List<Object> l = (List<Object>) newObj;
      // Using an iterator (shudder) in order to correctly remove elements.
      Iterator<Object> it = l.iterator();
      while (it.hasNext()) {
        Object item = it.next();
        if (removeNested(item, accessorList, referredId)) {
          it.remove();
        }
      }
      return false;
    }

    // If this is the last accessor, we're throwing out the entire object
    // keeping the reference...
    if (accessorList.size() == 1) {
      if (newObj instanceof BSONObject) {
        Object x = ((BSONObject) newObj).get(accessorList.get(0));
        if (x instanceof BSONObject) {
          if (((BSONObject) x).get("id").equals(referredId)) {
            return true;
          }
        }
      }
    } else if (newObj instanceof BSONObject) {
      // Recurse into objects:
      String k = accessorList.get(0);
      Object newerObj = ((BSONObject) newObj).get(k);
      if (removeNested(newerObj, accessorList.subList(1, accessorList.size()), referredId)) {
        ((BSONObject) newObj).removeField(k);
      }
    }
    return false;
  }

  private <T extends Document> void setNextId(Class<T> cls, T item) {
    BasicDBObject idFinder = new BasicDBObject("_id", MongoUtils.getCollectionName(cls));
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
      // This is really evil, but it's very annoying to update only the fields you want
      // (you can't ignore fields, only give an explicit list of everything you want)
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
    JacksonDBCollection<MongoChanges, String> versionCol = MongoUtils.getVersioningCollection(db, cls);
    long count = versionCol.count(DBQuery.is("_id", id));
    if (count != 0 && oldItem != null) {
      BSONObject newToOld = MongoDiff.diffDocuments(item, oldItem);
      BSONObject oldToNew = MongoDiff.diffDocuments(oldItem, item);
      changeVersionObj(id, newToOld, oldToNew, oldRev, versionCol);
    } else {
      versionCol.insert(new MongoChanges(id, MongoUtils.getObjectForDoc(item)));
    }
  }

  private <T extends Document> void changeVersionObj(String id, BSONObject fromnext, BSONObject frompast, int oldRev, JacksonDBCollection<MongoChanges, String> versionCol)
      throws MongoException {
    // Create an object with the changes:
    String oldChangeKey = "changes." + Integer.toString(oldRev) + ".fromnext";
    DBObject setProps = new BasicDBObject(oldChangeKey, fromnext);

    String changeKey = "changes." + Integer.toString(oldRev + 1);
    setProps.put(changeKey, new BasicDBObject("frompast", frompast));

    setProps.put("lastRev", oldRev + 1);

    // NB: the list pushes (by index) are technically concurrency-unsafe (ie,
    // suffer from ABA issues)
    // which we mitigate by using the rev check to verify that we're changing
    // what we think
    // we're changing. Mongo then guarantees the atomicity of the following
    // update:
    DBObject update = new BasicDBObject("$set", setProps);
    Query query = DBQuery.is("_id", id).is("lastRev", oldRev);
    WriteResult<MongoChanges, String> updateResult = versionCol.update(query, update);

    CommandResult cachedLastError = updateResult.getCachedLastError();
    if (!cachedLastError.ok() || updateResult.getN() != 1) {
      throw new MongoException("Updating the version table failed!\n" + cachedLastError.getErrorMessage());
    }
  }

}
