package nl.knaw.huygens.repository.storage.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.storage.BasicStorage;
import nl.knaw.huygens.repository.storage.GenericDBRef;
import nl.knaw.huygens.repository.storage.StorageConfiguration;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.StorageUtils;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;

public class MongoStorage extends MongoStorageBase implements BasicStorage {

  // private Set<String> versionedDocumentTypes;

  public MongoStorage(DocTypeRegistry registry, StorageConfiguration conf) throws UnknownHostException, MongoException {
    super(registry);
    dbName = conf.getDbName();
    mongo = new Mongo(conf.getHost(), conf.getPort());
    db = mongo.getDB(dbName);
    if (conf.requiresAuth()) {
      db.authenticate(conf.getUser(), conf.getPassword().toCharArray());
    }
    db.setWriteConcern(WriteConcern.SAFE);
    initializeDB(conf);
  }

  public MongoStorage(DocTypeRegistry registry, StorageConfiguration conf, Mongo m, DB loanedDB) {
    super(registry);
    mongo = m;
    db = loanedDB;
    initializeDB(conf);
  }

  private void initializeDB(StorageConfiguration conf) {
    counterCol = JacksonDBCollection.wrap(db.getCollection(COUNTER_COLLECTION_NAME), Counter.class, String.class);
    documentCollections = conf.getDocumentTypes();
    // versionedDocumentTypes = conf.getVersionedTypes();
  }

  @Override
  public <T extends Document> T getItem(Class<T> type, String id) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    return col.findOneById(id);
  }

  @Override
  public <T extends Document> T searchItem(Class<T> type, T example) throws IOException {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    BasicDBObject query = new BasicDBObject();

    Map<String, Object> searchProperties = new MongoObjectMapper().mapObject(type, example);

    Set<String> keys = searchProperties.keySet();

    for (String key : keys) {
      query.put(key, searchProperties.get(key));
    }

    return col.findOne(query);
  }

  @Override
  public <T extends Document> StorageIterator<T> getAllByType(Class<T> cls) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, cls);
    return new MongoDBIteratorWrapper<T>(col.find());
  }

  @Override
  public <T extends Document> MongoChanges<T> getAllRevisions(Class<T> type, String id) {
    return MongoUtils.getVersioningCollection(db, type).findOneById(id);
  }

  @Override
  public <T extends Document> StorageIterator<T> getByMultipleIds(Class<T> type, Collection<String> ids) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    return new MongoDBIteratorWrapper<T>(col.find(DBQuery.in("_id", ids)));
  }

  @Override
  public List<Document> getLastChanged(int limit) {
    List<Document> changedDocs = Lists.newArrayList();
    for (String colName : documentCollections) {
      JacksonDBCollection<? extends Document, String> col = MongoUtils.getCollection(db, docTypeRegistry.getTypeForIName(colName));
      changedDocs.addAll(col.find().sort(new BasicDBObject("^lastChange.dateStamp", -1)).limit(limit).toArray());
    }

    StorageUtils.sortDocumentsByLastChange(changedDocs);
    return changedDocs.subList(0, limit);
  }

  @Override
  public <T extends Document> void fetchAll(Class<T> type, List<GenericDBRef<T>> refs) {
    Set<String> mongoRefs = Sets.newHashSetWithExpectedSize(refs.size());
    for (GenericDBRef<T> ref : refs) {
      mongoRefs.add(ref.id);
    }
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
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
        ref.setItem(results.get(ref.id));
      }
    }
  }

  @Override
  public <T extends Document> List<String> getIdsForQuery(Class<T> cls, List<String> accessors, String[] ids) {
    JacksonDBCollection<T, String> collection = MongoUtils.getCollection(db, cls);
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
  public <T extends Document> void ensureIndex(Class<T> cls, List<List<String>> accessorList) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, cls);
    for (List<String> accessors : accessorList) {
      col.ensureIndex(getQueryStr(accessors));
    }
  }

  private String getQueryStr(List<String> accessors) {
    return Joiner.on(".").join(accessors) + ".id";
  }

  // -------------------------------------------------------------------
  // --- modifiable ----------------------------------------------------

  // TODO make unit test: add & retrieve
  @Override
  public <T extends Document> void addItem(Class<T> type, T item) throws IOException {
    item.setCreation(item.getLastChange());
    if (item.getId() == null) {
      setNextId(type, item);
    }
    MongoUtils.getCollection(db, type).insert(item);
  }

  //
  //  ** The code below is temporarily retained because it contains ideas about versioning **
  //
  //  public <T extends Document> void addItems(Class<T> type, List<T> items) throws IOException {
  //    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
  //    boolean shouldVersion = versionedDocumentTypes.contains(col.getName());
  //
  //    // Create the changes objects for all these documents:
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
    MongoUtils.getCollection(db, type).update(query, update);
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

  // -------------------------------------------------------------------

  private <T extends Document> void setNextId(Class<T> type, T item) {
    BasicDBObject idFinder = new BasicDBObject("_id", docTypeRegistry.getINameForType(type));
    BasicDBObject counterIncrement = new BasicDBObject("$inc", new BasicDBObject("next", 1));

    // Find by id, return all fields, use default sort, increment the counter,
    // return the new object, create if no object exists:
    Counter newCounter = counterCol.findAndModify(idFinder, null, null, false, counterIncrement, true, true);

    String newId = type.getAnnotation(IDPrefix.class).value() + String.format("%1$010d", newCounter.next);
    item.setId(newId);
  }

}
