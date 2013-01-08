package nl.knaw.huygens.repository.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;
import nl.knaw.huygens.repository.model.Change;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.IDMapper;
import nl.knaw.huygens.repository.model.IDPrefix;
import nl.knaw.huygens.repository.model.storage.GenericDBRef;
import nl.knaw.huygens.repository.model.storage.JsonViews;
import nl.knaw.huygens.repository.model.storage.Storage;
import nl.knaw.huygens.repository.model.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.MongoUtils.MongoChanges;

import org.bson.BSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

public class MongoDBStorage implements Storage {
  private Mongo mongo;
  private DB db;
  private String dbName;

  private final String COUNTER_COLLECTION_NAME = "counters";
  private final JacksonDBCollection<Counter, String> counterCol;

  private final List<String> documentCollections;
  private List<String> versionedDocumentTypes;

  private static class Counter {
    @SuppressWarnings("unused")
    @JsonProperty("_id")
    public String id;
    public int next;
  }

  public MongoDBStorage(StorageConfiguration conf) throws UnknownHostException, MongoException {
    dbName = conf.getDbName();
    mongo = new Mongo(conf.getHost(), conf.getPort());
    db = mongo.getDB(dbName);
    if (conf.requiresAuth()) {
      db.authenticate(conf.getUser(), conf.getPassword().toCharArray());
    }
    db.setWriteConcern(WriteConcern.SAFE);
    counterCol = JacksonDBCollection.wrap(db.getCollection(COUNTER_COLLECTION_NAME), Counter.class, String.class);
    documentCollections = conf.getDocumentTypes();
    versionedDocumentTypes = conf.getVersionedTypes();
  }

  @Override
  public <T extends Document> T getItem(String id, Class<T> cls) {
    JacksonDBCollection<T, String> col = getCollection(cls);
    return col.findOneById(id);
  }

  @Override
  public <T extends Document> StorageIterator<T> getAllByType(Class<T> cls) {
    JacksonDBCollection<T, String> col = getCollection(cls);
    return new MongoDBIteratorWrapper<T>(col.find());
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
    JacksonDBCollection<T, String> col = getCollection(cls);
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
      counterCol.findAndModify(counterQuery, null, null, false,
          new BasicDBObject("$set", new BasicDBObject("next", lastId)), false, true);
    }
    // Insert the items:
    col.insert(items);
    // Insert the changes:
    if (shouldVersion) {
      JacksonDBCollection<MongoChanges, String> versionCol = getVersioningCollection(cls);
      versionCol.insert(changes);
    }
  }

  @Override
  public <T extends Document> void updateItem(String id, T updatedItem, Class<T> cls) throws IOException {
    addVersion(id, updatedItem, cls);
    updatedItem.setRev(updatedItem.getRev() + 1);
  }

  @Override
  public <T extends Document> void deleteItem(String id, Class<T> cls, Change change) throws IOException {
    JacksonDBCollection<T, String> col = getCollection(cls);
    // This needs to be updated once mongo-jackson-mapper fixes their wrapper:
    // Update the actual document first:
    BasicDBObject settings = new BasicDBObject("^deleted", true);
    DBObject newLastChange = MongoUtils.getObjectForDoc(change);
    settings.put("^lastChange", newLastChange);
    BasicDBObject update = new BasicDBObject("$set", settings);
    update.put("$inc", new BasicDBObject("^rev", 1));
    // This returns the previous version of the document (!)
    // NB: we don't check the rev prop here. This is because deletion will always work;
    // we simply set the delete prop to true.
    T item = col.findAndModify(DBQuery.is("_id", id), update);

    // Then update the versioning table:
    JacksonDBCollection<MongoChanges, String> versionCol = getVersioningCollection(cls);
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
  public MongoChanges getAllRevisions(String id, Class<? extends Document> baseCls) {
    JacksonDBCollection<MongoChanges, String> versionCol = getVersioningCollection(baseCls);
    MongoChanges item = versionCol.findOneById(id);
    return item;
  }

  @Override
  public void destroy() {
    db.cleanCursors(true);
    mongo.close();
    System.err.println("Stopped Mongo.");
  }


  @Override
  public <T extends Document> StorageIterator<T> getByMultipleIds(List<String> ids, Class<T> entityCls) {
    JacksonDBCollection<T, String> col = getCollection(entityCls);
    return new MongoDBIteratorWrapper<T>(col.find(DBQuery.in("_id", ids)));
  }

  @Override
  public List<Document> getLastChanged(int limit) {
    List<Document> changedDocs = Lists.newArrayList();
    for (String colName : documentCollections) {
      JacksonDBCollection<? extends Document, String> col = getCollection(Document.getSubclassByString(colName));
      changedDocs.addAll(col.find().sort(new BasicDBObject("^lastChange.dateStamp", -1)).limit(limit).toArray());
    }

    Collections.sort(changedDocs, new Comparator<Document>() {
      @Override
      public int compare(Document o1, Document o2) {
        long o1s = o1 != null && o1.getLastChange() != null ? o1.getLastChange().dateStamp : 0;
        long o2s = o2 != null && o2.getLastChange() != null ? o2.getLastChange().dateStamp : 0;
        long d = o2s - o1s;
        return d > 0 ? 1 : (d < 0 ? -1 : 0);
      }
    });
    return changedDocs.subList(0, limit);
  }

  @Override
  public void empty() {
    db.cleanCursors(true);
    mongo.dropDatabase(dbName);
    db = mongo.getDB(dbName);
  }

  // FIXME: THESE SHOULD BE ON A UTILITY CLASS
  @Override
public <T extends Document> List<T> resolveIterator(StorageIterator<T> it, int limit) {
    return resolveIterator(it, 0, limit);
  }
  @Override
public <T extends Document> List<T> resolveIterator(StorageIterator<T> it, int offset, int limit) {
    if (offset > 0) {
      it.skip(offset);
    }
    List<T> rv = it.getSome(limit);
    it.close();
    return rv;
  }

  @Override
public <T extends Document> List<T> readFromIterator(StorageIterator<T> it, int limit) {
    return readFromIterator(it, 0, limit);
  }
  @Override
public <T extends Document> List<T> readFromIterator(StorageIterator<T> it, int offset, int limit) {
    if (offset > 0) {
      it.skip(offset);
    }
    return it.getSome(limit);
  }

  // FIXME this should probably on the index.
  // FIXME Separately, we should investigate how to optimize
  // client-server sync wrt autocomplete fields etc. when there
  // are DB updates.
  @Override
  public Map<String, String> getSimpleMap(Class<? extends Document> cls) {
    JacksonDBCollection<? extends Document,String> collection = getCollection(cls);
    DBCursor<? extends Document> cursor = collection.find();
    Map<String, String> rv = Maps.newHashMapWithExpectedSize(cursor.count());
    try {
      while (cursor.hasNext()) {
        Document d = cursor.next();
        d.fetchAll(this);
        rv.put(d.getId(), d.getDescription());
      }
    } finally {
      cursor.close();
    }
    return rv;
  }

  @Override
  public <T extends Document> void fetchAll(List<GenericDBRef<T>> refs, Class<T> cls) {
    Set<String> mongoRefs = Sets.newHashSetWithExpectedSize(refs.size());
    for (GenericDBRef<T> ref : refs) {
      mongoRefs.add(ref.getId());
    }
    JacksonDBCollection<T, String> col = getCollection(cls);
    Map<String, T> results = Maps.newHashMapWithExpectedSize(mongoRefs.size());
    DBCursor<T> resultCursor = col.find(DBQuery.in("_id", mongoRefs));
    try {
      while (resultCursor.hasNext()) {
        T doc = resultCursor.next();
        results.put(doc.getId(), doc);
      }
    } finally {
      resultCursor.close();
    }
    for (GenericDBRef<T> ref : refs) {
      if (ref.getItem() == null) {
        ref.setItem(results.get(ref.getId()));
      }
    }
  }

  @Override
  public <T extends Document> List<String> getIdsForQuery(Class<T> cls, List<String> accessors, String[] ids) {
    JacksonDBCollection<T,String> collection = getCollection(cls);
    String queryStr = getQueryStr(accessors);
    DBObject query;
    if (ids.length == 1) {
      query = new BasicDBObject(queryStr, ids[0]);
    } else {
      query = DBQuery.in(queryStr, (Object[]) ids);
    }
    List<String> items = Lists.newArrayList();
    DBCursor<T> resultCursor = collection.find(query, null);
    try {
      while (resultCursor.hasNext()) {
        items.add(resultCursor.next().getId());
      }
    } finally {
      resultCursor.close();
    }
    return items;
  }

  @Override
public void ensureIndex(Class<? extends Document> cls, List<List<String>> accessorList) {
    JacksonDBCollection<? extends Document, String> col = getCollection(cls);
    for (List<String> accessors : accessorList) {
      col.ensureIndex(getQueryStr(accessors));
    }
  }

  @Override
  public <T extends Document> void removeReference(Class<T> cls, List<String> accessorList, List<String> referringIds, String referredId, Change change) {
    JacksonDBCollection<T, String> col = getCollection(cls);
    JacksonDBCollection<MongoChanges, String> versionCol = getVersioningCollection(cls);
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
      changeVersionObj(doc.getId(), MongoUtils.bsondiff(newObj, origObj), MongoUtils.bsondiff(origObj, newObj), doc.getRev(), versionCol);
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

    // If this is the last accessor, we're throwing out the entire object keeping the reference...
    if (accessorList.size() == 1) {
      if (newObj instanceof BSONObject) {
        Object x = ((BSONObject) newObj).get(accessorList.get(0));
        if (x instanceof BSONObject) {
          if (((BSONObject)x).get("id").equals(referredId)) {
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

  private String getQueryStr(List<String> accessors) {
    return Joiner.on(".").join(accessors) + ".id";
  }

  private <T extends Document> void setNextId(Class<T> cls, T item) {
    BasicDBObject idFinder = new BasicDBObject("_id", MongoUtils.getCollectionName(cls));
    BasicDBObject counterIncrement = new BasicDBObject("$inc", new BasicDBObject("next", 1));
    
    // Find by id, return all fields, use default sort, increment the counter, return the new object, create if no object exists:
    Counter newCounter = counterCol.findAndModify(idFinder, null, null, false, counterIncrement, true, true);
    
    String newId = cls.getAnnotation(IDPrefix.class).value() + String.format("%1$010d", newCounter.next);
    item.setId(newId);
  }

  private <T extends Document> void addVersion(String id, T item, Class<T> cls) throws IOException {
    addVersion(id, item, cls, false);
  }

  private <T extends Document> void addVersion(String id, T item, Class<T> cls, boolean doInsert) throws IOException {
    JacksonDBCollection<T, String> col = getCollection(cls);
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
      // This is really evil, but it's very annoying to update only the fields you want (you can't ignore fields,
      // only give an explicit list of everything you want)
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
    JacksonDBCollection<MongoChanges, String> versionCol = getVersioningCollection(cls);
    long count = versionCol.count(DBQuery.is("_id", id));
    if (count != 0 && oldItem != null) {
      changeVersionObj(id, MongoUtils.diff(item,  oldItem), MongoUtils.diff(oldItem,  item), oldRev, versionCol);
    } else {
      versionCol.insert(new MongoChanges(id, MongoUtils.getObjectForDoc(item)));
    }
  }

  private <T extends Document> void changeVersionObj(String id, BSONObject fromnext, BSONObject frompast, int oldRev, JacksonDBCollection<MongoChanges, String> versionCol) throws MongoException {
    DBObject setProps = new BasicDBObject("changes." + Integer.toString(oldRev) + ".fromnext", fromnext);
    setProps.put("lastRev", oldRev + 1);
    setProps.put("changes." + Integer.toString(oldRev + 1), new BasicDBObject("frompast", frompast));
    DBObject update = new BasicDBObject("$set", setProps);
    // NB: the list pushes are technically concurrency-unsafe (ie, suffer from ABA issues)
    // which we mitigate by using the rev check to verify that we're changing what we think
    // we're changing. Mongo then guarantees the atomicity of the following update:
    WriteResult<MongoChanges, String> updateResult = versionCol.update(DBQuery.is("_id", id).is("lastRev", oldRev), update);
    CommandResult cachedLastError = updateResult.getCachedLastError();
    if (!cachedLastError.ok() || updateResult.getN() != 1) {
      throw new MongoException("Updating the version table failed!\n" + cachedLastError.getErrorMessage());
    }
  }


  private <T extends Document> JacksonDBCollection<T, String> getCollection(Class<T> cls) {
    DBCollection col = db.getCollection(MongoUtils.getCollectionName(cls));
    return JacksonDBCollection.wrap(col, cls, String.class, JsonViews.DBView.class);
  }

  private <T extends Document> JacksonDBCollection<MongoChanges, String> getVersioningCollection(Class<T> cls) {
    DBCollection col = db.getCollection(MongoUtils.getVersioningCollectionName(cls));
    return JacksonDBCollection.wrap(col, MongoChanges.class, String.class, JsonViews.DBView.class);
  }
}
