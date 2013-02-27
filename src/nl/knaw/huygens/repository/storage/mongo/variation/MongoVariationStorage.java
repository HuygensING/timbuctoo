package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

import net.vz.mongodb.jackson.DBQuery;
import net.vz.mongodb.jackson.JacksonDBCollection;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.generic.GenericDBRef;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.mongo.MongoChanges;
import nl.knaw.huygens.repository.storage.mongo.MongoUtils;
import nl.knaw.huygens.repository.variation.VariationException;
import nl.knaw.huygens.repository.variation.VariationReducer;
import nl.knaw.huygens.repository.variation.VariationUtils;

public abstract class MongoVariationStorage implements Storage {
  protected Mongo mongo;
  protected DB db;
  protected String dbName;

  private final String COUNTER_COLLECTION_NAME = "counters";
  protected JacksonDBCollection<Counter, String> counterCol;

  private Set<String> documentCollections;
  
  private VariationReducer reducer;
  private MongoOptions options;

  static class Counter {
    @JsonProperty("_id")
    public String id;
    public int next;
  }
  
  public MongoVariationStorage(StorageConfiguration conf) throws UnknownHostException, MongoException {
    dbName = conf.getDbName();
    options = new MongoOptions();
    options.safe = true;
    options.dbDecoderFactory = new TreeDecoderFactory();
    options.dbEncoderFactory = new TreeEncoderFactory(new ObjectMapper());    mongo = new Mongo(new ServerAddress(conf.getHost(), conf.getPort()), options);
    db = mongo.getDB(dbName);
    if (conf.requiresAuth()) {
      db.authenticate(conf.getUser(), conf.getPassword().toCharArray());
    }
    initializeVariationCollections(conf);
  }
  
  public MongoVariationStorage(StorageConfiguration conf, Mongo m, DB db, MongoOptions options) throws UnknownHostException, MongoException {
    this.options = options;
    dbName = conf.getDbName();
    this.mongo = m;
    this.db = db;
    initializeVariationCollections(conf);
  }

  private void initializeVariationCollections(StorageConfiguration conf) {
    counterCol = JacksonDBCollection.wrap(db.getCollection(COUNTER_COLLECTION_NAME), Counter.class, String.class);
    documentCollections = conf.getDocumentTypes();
    reducer = new VariationReducer();
  }

  @Override
  public <T extends Document> T getItem(String id, Class<T> cls) throws VariationException, IOException {
    DBCollection col = getRawCollection(cls);
    DBObject query = new BasicDBObject("_id", id);
    return reducer.reduceDBObject(col.findOne(query), cls);
  }

  @Override
  public <T extends Document> StorageIterator<T> getAllByType(Class<T> cls) {
    DBCollection col = getRawCollection(cls);
    String t = MongoUtils.getCollectionName(VariationUtils.getFirstCommonClass(cls));
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
  public <T extends Document> StorageIterator<T> getByMultipleIds(Collection<String> ids, Class<T> entityCls) {
    DBCollection col = getRawCollection(entityCls);
    return new MongoDBVariationIteratorWrapper<T>(col.find(DBQuery.in("_id", ids)), reducer, entityCls);
  }

  @Override
  public List<Document> getLastChanged(int limit) throws IOException {
    List<DBObject> changedDocs = Lists.newArrayList();
    for (String colName : documentCollections) {
      DBCollection col  = db.getCollection(colName);
      DBCursor docs = col.find().sort(new BasicDBObject("^lastChange.dateStamp", -1)).limit(limit);
      changedDocs.addAll(docs.toArray());
      docs.close();
    }
    
    MongoUtils.sortDocumentsByLastChange(changedDocs);
    return reducer.reduceDBObject(changedDocs.subList(0, limit), Document.class);
  }

  @Override
  public <T extends Document> void fetchAll(List<GenericDBRef<T>> refs, Class<T> cls) {
    Set<String> mongoRefs = Sets.newHashSetWithExpectedSize(refs.size());
    for (GenericDBRef<T> ref : refs) {
      mongoRefs.add(ref.id);
    }
    DBCollection col = getRawCollection(cls);
    Map<String, T> results = Maps.newHashMapWithExpectedSize(mongoRefs.size());
    DBCursor resultCursor = col.find(DBQuery.in("_id", mongoRefs));
    MongoDBVariationIteratorWrapper<T> it = new MongoDBVariationIteratorWrapper<T>(resultCursor, reducer, cls);
    while (it.hasNext()) {
      T doc = it.next();
      results.put(doc.getId(), doc);
    }
    it.close();
    for (GenericDBRef<T> ref : refs) {
      if (ref.getItem() == null) {
        ref.setItem(results.get(ref.id));
      }
    }
  }

  @Override
  public <T extends Document> List<String> getIdsForQuery(Class<T> cls, List<String> accessors, String[] ids) {
    DBCollection collection = getRawCollection(cls);
    String queryStr = getQueryStr(accessors);
    DBObject query;
    if (ids.length == 1) {
      query = new BasicDBObject(queryStr, ids[0]);
    } else {
      query = DBQuery.in(queryStr, (Object[]) ids);
    }
    List<String> items = Lists.newArrayList();
    DBCursor resultCursor = collection.find(query, new BasicDBObject());
    try {
      while (resultCursor.hasNext()) {
        items.add((String) resultCursor.next().get("_id"));
      }
    } finally {
      resultCursor.close();
    }
    return items;
  }

  @Override
  public void ensureIndex(Class<? extends Document> cls, List<List<String>> accessorList) {
    DBCollection col = getRawCollection(cls);
    for (List<String> accessors : accessorList) {
      col.ensureIndex(getQueryStr(accessors));
    }
  }

  private String getQueryStr(List<String> accessors) {
    return Joiner.on(".").join(accessors) + ".id";
  }

  protected DBCollection getRawCollection(Class<? extends Document> cls) {
    DBCollection col = db.getCollection(MongoUtils.getCollectionName(VariationUtils.getBaseClass(cls)));
    col.setDBEncoderFactory(options.dbEncoderFactory);
    return col;
  }
}
