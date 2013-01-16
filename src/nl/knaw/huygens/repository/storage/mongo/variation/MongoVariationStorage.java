package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.JacksonDBCollection;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.generic.GenericDBRef;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.generic.StorageUtils;
import nl.knaw.huygens.repository.storage.mongo.MongoChanges;
import nl.knaw.huygens.repository.storage.mongo.MongoUtils;
import nl.knaw.huygens.repository.variation.VariationException;
import nl.knaw.huygens.repository.variation.VariationReducer;
import nl.knaw.huygens.repository.variation.VariationUtils;

// FIXME: this is a WIP, and needs to essentially stop using MongoUtils.getCollection and MongoUtils.getVersioningCollection.
// instead, it should use a 'real' DBCollection, and wrap the results from there.
public abstract class MongoVariationStorage implements Storage {
  protected Mongo mongo;
  protected DB db;
  protected String dbName;

  private final String COUNTER_COLLECTION_NAME = "counters";
  protected final JacksonDBCollection<Counter, String> counterCol;

  private final List<String> documentCollections;
  protected List<String> versionedDocumentTypes;
  
  private final VariationReducer reducer;

  static class Counter {
    @JsonProperty("_id")
    public String id;
    public int next;
  }

  public MongoVariationStorage(StorageConfiguration conf) throws UnknownHostException, MongoException {
    dbName = conf.getDbName();
    mongo = new Mongo(conf.getHost(), conf.getPort());
    
    MongoOptions options = new MongoOptions();
    options.safe = true;
    options.dbDecoderFactory = new TreeDecoderFactory();
    mongo = new Mongo(new ServerAddress(conf.getHost(), conf.getPort()), options);
    db = mongo.getDB(dbName);
    if (conf.requiresAuth()) {
      db.authenticate(conf.getUser(), conf.getPassword().toCharArray());
    }
    counterCol = JacksonDBCollection.wrap(db.getCollection(COUNTER_COLLECTION_NAME), Counter.class, String.class);
    documentCollections = conf.getDocumentTypes();
    versionedDocumentTypes = conf.getVersionedTypes();
    reducer = new VariationReducer();
  }

  @Override
  public <T extends Document> T getItem(String id, Class<T> cls) throws VariationException, IOException {
    DBCollection col  = db.getCollection(MongoUtils.getCollectionName(VariationUtils.getBaseClass(cls)));
    DBObject query = new BasicDBObject("_id", id);
    return reducer.reduceDBObject(col.findOne(query), cls);
  }

  @Override
  public <T extends Document> StorageIterator<T> getAllByType(Class<T> cls) {
    DBCollection col = db.getCollection(MongoUtils.getCollectionName(VariationUtils.getBaseClass(cls)));
    String t = MongoUtils.getCollectionName(VariationUtils.getEarliestCommonClass(cls));
    DBObject query = new BasicDBObject("^type", t);
    return new MongoDBVariationIteratorWrapper<T>(col.find(query), reducer, cls);
  }

  @Override
  public MongoChanges getAllRevisions(String id, Class<? extends Document> baseCls) {
    return MongoUtils.getVersioningCollection(db, baseCls).findOneById(id);
  }

  @Override
  public void destroy() {
    db.cleanCursors(true);
    mongo.close();
    System.err.println("Stopped Mongo.");
  }


  @Override
  public <T extends Document> StorageIterator<T> getByMultipleIds(List<String> ids, Class<T> entityCls) {
    DBCollection col = db.getCollection(MongoUtils.getCollectionName(VariationUtils.getBaseClass(entityCls)));
    return new MongoDBVariationIteratorWrapper<T>(col.find(DBQuery.in("_id", ids)), reducer, entityCls);
  }

  @Override
  public List<Document> getLastChanged(int limit) {
    List<Document> changedDocs = Lists.newArrayList();
    for (String colName : documentCollections) {
      JacksonDBCollection<? extends Document, String> col = MongoUtils.getCollection(db, Document.getSubclassByString(colName));
      changedDocs.addAll(col.find().sort(new BasicDBObject("^lastChange.dateStamp", -1)).limit(limit).toArray());
    }
    
    StorageUtils.sortDocumentsByLastChange(changedDocs);
    return changedDocs.subList(0, limit);
  }

  // FIXME this should probably on the index.
  // FIXME Separately, we should investigate how to optimize
  // client-server sync wrt autocomplete fields etc. when there
  // are DB updates.
  @Override
  public Map<String, String> getSimpleMap(Class<? extends Document> cls) {
    JacksonDBCollection<? extends Document,String> collection = MongoUtils.getCollection(db, cls);
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
      mongoRefs.add(ref.id);
    }
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, cls);
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
    JacksonDBCollection<T,String> collection = MongoUtils.getCollection(db, cls);
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
    JacksonDBCollection<? extends Document, String> col = MongoUtils.getCollection(db, cls);
    for (List<String> accessors : accessorList) {
      col.ensureIndex(getQueryStr(accessors));
    }
  }

  private String getQueryStr(List<String> accessors) {
    return Joiner.on(".").join(accessors) + ".id";
  }

}
