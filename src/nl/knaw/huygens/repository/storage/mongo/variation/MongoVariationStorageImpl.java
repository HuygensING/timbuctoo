package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.generic.GenericDBRef;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.mongo.MongoChanges;
import nl.knaw.huygens.repository.storage.mongo.MongoUtils;
import nl.knaw.huygens.repository.variation.VariationException;
import nl.knaw.huygens.repository.variation.VariationReducer;
import nl.knaw.huygens.repository.variation.VariationUtils;

import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

@Singleton
public abstract class MongoVariationStorageImpl implements MongoVariationStorage {

  protected Mongo mongo;
  protected DB db;
  protected String dbName;

  private final String COUNTER_COLLECTION_NAME = "counters";
  protected JacksonDBCollection<Counter, String> counterCol;

  private Set<String> documentCollections;

  private VariationReducer reducer;
  protected MongoOptions options;

  private ObjectMapper objectMapper;
  protected TreeEncoderFactory treeEncoderFactory;
  private TreeDecoderFactory treeDecoderFactory;

  static class Counter {
    @JsonProperty("_id")
    public String id;
    public int next;
  }

  private Map<Class<? extends Document>, DBCollection> collectionCache;
  protected final DocTypeRegistry docTypeRegistry;

  public MongoVariationStorageImpl(StorageConfiguration conf, DocTypeRegistry docTypeRegistry) throws UnknownHostException, MongoException {
    this.docTypeRegistry = docTypeRegistry;
    dbName = conf.getDbName();
    options = new MongoOptions();
    options.safe = true;
    mongo = new Mongo(new ServerAddress(conf.getHost(), conf.getPort()), options);
    db = mongo.getDB(dbName);
    if (conf.requiresAuth()) {
      db.authenticate(conf.getUser(), conf.getPassword().toCharArray());
    }
    initializeVariationCollections(conf);
  }

  public MongoVariationStorageImpl(StorageConfiguration conf, Mongo m, DB db, MongoOptions options, DocTypeRegistry docTypeRegistry) throws UnknownHostException, MongoException {
    this.options = options;
    this.docTypeRegistry = docTypeRegistry;
    dbName = conf.getDbName();
    this.mongo = m;
    this.db = db;
    initializeVariationCollections(conf);
  }

  private void initializeVariationCollections(StorageConfiguration conf) {
    objectMapper = new ObjectMapper();
    treeEncoderFactory = new TreeEncoderFactory(objectMapper);
    treeDecoderFactory = new TreeDecoderFactory();
    collectionCache = Maps.newHashMap();
    counterCol = JacksonDBCollection.wrap(db.getCollection(COUNTER_COLLECTION_NAME), Counter.class, String.class);
    documentCollections = conf.getDocumentTypes();
    reducer = new VariationReducer(docTypeRegistry);
  }

  @Override
  public <T extends Document> T getItem(Class<T> type, String id) throws VariationException, IOException {
    DBCollection col = getVariationCollection(type);
    DBObject query = new BasicDBObject("_id", id);
    addClassNotNull(type, query);
    return reducer.reduceDBObject(col.findOne(query), type);
  }

  private <T extends Document> void addClassNotNull(Class<T> type, DBObject query) {
    String classType = VariationUtils.getClassId(type);
    BasicDBObject notNull = new BasicDBObject("$ne", null);
    query.put(classType, notNull);
  }

  @Override
  public <T extends Document> List<T> getAllVariations(Class<T> type, String id) throws VariationException, IOException {
    DBCollection col = getVariationCollection(type);
    DBObject query = new BasicDBObject("_id", id);
    DBObject item = col.findOne(query);
    return reducer.getAllForDBObject(item, type);
  }

  @Override
  public <T extends DomainDocument> T getVariation(Class<T> type, String id, String variation) throws IOException {
    DBCollection col = getVariationCollection(type);
    DBObject query = new BasicDBObject("_id", id);
    addClassNotNull(type, query);
    return reducer.reduceDBObject(col.findOne(query), type, variation);
  }

  @Override
  public <T extends Document> StorageIterator<T> getAllByType(Class<T> cls) {
    DBCollection col = getVariationCollection(cls);
    String classType = VariationUtils.getClassId(cls);
    BasicDBObject notNull = new BasicDBObject("$ne", null);
    BasicDBObject query = new BasicDBObject(classType, notNull);
    return new MongoDBVariationIteratorWrapper<T>(col.find(query), reducer, cls);
  }

  @Override
  public <T extends Document> MongoChanges<T> getAllRevisions(Class<T> type, String id) {
    return MongoUtils.getVersioningCollection(db, type).findOneById(id);
  }

  @Override
  public void destroy() {
    db.cleanCursors(true);
    mongo.close();
    System.err.println("Stopped Mongo.");
  }

  @Override
  public <T extends Document> StorageIterator<T> getByMultipleIds(Class<T> type, Collection<String> ids) {
    DBCollection col = getVariationCollection(type);
    return new MongoDBVariationIteratorWrapper<T>(col.find(DBQuery.in("_id", ids)), reducer, type);
  }

  @Override
  public List<Document> getLastChanged(int limit) throws IOException {
    List<DBObject> changedDocs = Lists.newArrayList();
    for (String colName : documentCollections) {
      DBCollection col = db.getCollection(colName);
      DBCursor docs = col.find().sort(new BasicDBObject("^lastChange.dateStamp", -1)).limit(limit);
      changedDocs.addAll(docs.toArray());
      docs.close();
    }

    MongoUtils.sortDocumentsByLastChange(changedDocs);
    return reducer.reduceDBObject(changedDocs.subList(0, limit), Document.class);
  }

  @Override
  public <T extends Document> void fetchAll(Class<T> type, List<GenericDBRef<T>> refs) {
    Set<String> mongoRefs = Sets.newHashSetWithExpectedSize(refs.size());
    for (GenericDBRef<T> ref : refs) {
      mongoRefs.add(ref.id);
    }
    DBCollection col = getVariationCollection(type);
    Map<String, T> results = Maps.newHashMapWithExpectedSize(mongoRefs.size());
    DBCursor resultCursor = col.find(DBQuery.in("_id", mongoRefs));
    MongoDBVariationIteratorWrapper<T> it = new MongoDBVariationIteratorWrapper<T>(resultCursor, reducer, type);
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
    DBCollection collection = getVariationCollection(cls);
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
  public <T extends Document> void ensureIndex(Class<T> type, List<List<String>> accessorList) {
    DBCollection col = getVariationCollection(type);
    for (List<String> accessors : accessorList) {
      col.ensureIndex(getQueryStr(accessors));
    }
  }

  private String getQueryStr(List<String> accessors) {
    return Joiner.on(".").join(accessors) + ".id";
  }

  protected <T extends Document> DBCollection getVariationCollection(Class<T> type) {
    DBCollection col;
    if (!collectionCache.containsKey(type)) {
      col = db.getCollection(docTypeRegistry.getCollectionId(type));
      col.setDBDecoderFactory(treeDecoderFactory);
      col.setDBEncoderFactory(treeEncoderFactory);
      collectionCache.put(type, col);
    } else {
      col = collectionCache.get(type);
    }
    return col;
  }

  protected <T extends Document> DBCollection getRawVersionCollection(Class<T> type) {
    DBCollection col = db.getCollection(docTypeRegistry.getCollectionId(type) + "-versions");
    col.setDBEncoderFactory(options.dbEncoderFactory);
    return col;
  }

}
