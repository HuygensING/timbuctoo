package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.VariationStorage;
import nl.knaw.huygens.repository.storage.generic.GenericDBRef;
import nl.knaw.huygens.repository.storage.generic.JsonViews;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.mongo.MongoChanges;
import nl.knaw.huygens.repository.storage.mongo.MongoStorageBase;
import nl.knaw.huygens.repository.storage.mongo.MongoUtils;
import nl.knaw.huygens.repository.variation.VariationException;
import nl.knaw.huygens.repository.variation.VariationInducer;
import nl.knaw.huygens.repository.variation.VariationReducer;
import nl.knaw.huygens.repository.variation.VariationUtils;

import org.apache.commons.lang.NotImplementedException;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

public class MongoVariationStorage extends MongoStorageBase implements VariationStorage {

  private VariationInducer inducer;
  private VariationReducer reducer;
  private MongoOptions options;

  private ObjectMapper objectMapper;
  private TreeEncoderFactory treeEncoderFactory;
  private TreeDecoderFactory treeDecoderFactory;

  private Map<Class<? extends Document>, DBCollection> collectionCache;

  public MongoVariationStorage(StorageConfiguration conf, DocTypeRegistry registry) throws UnknownHostException, MongoException {
    super(registry);
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

  public MongoVariationStorage(StorageConfiguration conf, Mongo m, DB db, MongoOptions options, DocTypeRegistry registry) throws UnknownHostException, MongoException {
    super(registry);
    this.options = options;
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
    inducer = new VariationInducer();
    inducer.setView(JsonViews.DBView.class);
    reducer = new VariationReducer(docTypeRegistry);
  }

  @Override
  public <T extends Document> T getItem(Class<T> type, String id) throws VariationException, IOException {
    DBCollection col = getVariationCollection(type);
    DBObject query = new BasicDBObject("_id", id);
    addClassNotNull(type, query);
    return reducer.reduceDBObject(col.findOne(query), type);
  }

  @Override
  public <T extends Document> T searchItem(Class<T> type, T example) throws IOException {
    throw new NotImplementedException("This method is not intended to get used.");
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
  public <T extends Document> MongoChanges<T> getAllRevisions(Class<T> type, String id) throws IOException {
    DBCollection col = getRawVersionCollection(type);
    DBObject query = new BasicDBObject("_id", id);

    DBObject allRevisions = col.findOne(query);

    return reducer.reduceMultipleRevisions(type, allRevisions);
  }

  @Override
  public <T extends DomainDocument> T getRevision(Class<T> type, String id, int revisionId) throws IOException {
    DBCollection col = getRawVersionCollection(type);
    DBObject query = new BasicDBObject("_id", id);
    query.put("versions.^rev", revisionId);
    return reducer.reduceRevision(type, col.findOne(query));
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
    //col.setDBEncoderFactory(options.dbEncoderFactory);
    col.setDBDecoderFactory(treeDecoderFactory);
    col.setDBEncoderFactory(treeEncoderFactory);
    return col;
  }

  // -------------------------------------------------------------------

  @Override
  public <T extends Document> void addItem(Class<T> type, T item) throws IOException {
    if (item.getId() == null) {
      setNextId(type, item);
    }
    JsonNode jsonNode = inducer.induce(item, type);
    DBCollection col = getVariationCollection(type);
    JacksonDBObject<JsonNode> insertedItem = new JacksonDBObject<JsonNode>(jsonNode, JsonNode.class);
    col.insert(insertedItem);
    addInitialVersion(type, item.getId(), insertedItem);
  }

  @Override
  public <T extends Document> void updateItem(Class<T> type, String id, T item) throws IOException {
    DBCollection col = getVariationCollection(type);
    BasicDBObject q = new BasicDBObject("_id", id);
    q.put("^rev", item.getRev());
    DBObject existingNode = col.findOne(q);
    if (existingNode == null) {
      throw new IOException("No document was found for ID " + id + " and revision " + String.valueOf(item.getRev()) + " !");
    }
    JsonNode updatedNode = inducer.induce(item, type, existingNode);
    ((ObjectNode) updatedNode).put("^rev", item.getRev() + 1);
    JacksonDBObject<JsonNode> updatedDBObj = new JacksonDBObject<JsonNode>(updatedNode, JsonNode.class);
    col.update(q, updatedDBObj);
    addVersion(type, id, updatedDBObj);
  }

  @Override
  public <T extends Document> void setPID(Class<T> cls, String pid, String id) {
    BasicDBObject query = new BasicDBObject("_id", id);
    BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("^pid", pid));
    DBCollection col = getVariationCollection(cls);

    col.update(query, update);
  }

  @Override
  public <T extends Document> void deleteItem(Class<T> type, String id, Change change) throws IOException {
    DBCollection col = getVariationCollection(type);
    BasicDBObject q = new BasicDBObject("_id", id);
    DBObject existingNode = col.findOne(q);
    if (existingNode == null) {
      throw new IOException("No document was found for ID " + id + "!");
    }
    ObjectNode node;
    try {
      DBJsonNode realNode = (DBJsonNode) existingNode;
      JsonNode jsonNode = realNode.getDelegate();
      if (!jsonNode.isObject()) {
        throw new Exception();
      }
      node = (ObjectNode) jsonNode;
    } catch (Exception ex) {
      throw new IOException("Couldn't read properly from database.");
    }
    JsonNode changeTree = getMapper().valueToTree(change);
    node.put("^deleted", true).put("^lastChange", changeTree);
    int rev = node.get("^rev").asInt();
    node.put("^rev", rev + 1);
    q.put("^rev", rev);
    JacksonDBObject<JsonNode> updatedNode = new JacksonDBObject<JsonNode>(node, JsonNode.class);
    col.update(q, updatedNode);
    addVersion(type, id, updatedNode);
  }

  private <T extends Document> void addInitialVersion(Class<T> cls, String id, JacksonDBObject<JsonNode> initialVersion) {
    DBCollection col = getRawVersionCollection(cls);
    JsonNode actualVersion = initialVersion.getObject();

    ObjectMapper mapper = getMapper();
    ArrayNode versionsNode = mapper.createArrayNode();
    versionsNode.add(actualVersion);

    ObjectNode itemNode = mapper.createObjectNode();
    itemNode.put("versions", versionsNode);
    itemNode.put("_id", id);

    col.insert(new JacksonDBObject<JsonNode>(itemNode, JsonNode.class));
  }

  private <T extends Document> void addVersion(Class<T> cls, String id, JacksonDBObject<JsonNode> newVersion) {
    DBCollection col = getRawVersionCollection(cls);
    JsonNode actualVersion = newVersion.getObject();

    ObjectMapper mapper = getMapper();
    ObjectNode versionNode = mapper.createObjectNode();
    versionNode.put("versions", actualVersion);

    ObjectNode update = mapper.createObjectNode();
    update.put("$push", versionNode);

    col.update(new BasicDBObject("_id", id), new JacksonDBObject<JsonNode>(update, JsonNode.class));
  }

  private ObjectMapper getMapper() {
    return treeEncoderFactory.getObjectMapper();
  }

  private <T extends Document> void setNextId(Class<T> cls, T item) {
    BasicDBObject idFinder = new BasicDBObject("_id", docTypeRegistry.getCollectionId(cls));
    BasicDBObject counterIncrement = new BasicDBObject("$inc", new BasicDBObject("next", 1));

    // Find by id, return all fields, use default sort, increment the counter,
    // return the new object, create if no object exists:
    Counter newCounter = counterCol.findAndModify(idFinder, null, null, false, counterIncrement, true, true);

    String newId = getClassPrefix(cls) + String.format("%1$010d", newCounter.next);
    item.setId(newId);
  }

  private String getClassPrefix(Class<?> cls) {
    while (cls != null && !cls.equals(Document.class)) {
      IDPrefix annotation = cls.getAnnotation(IDPrefix.class);
      if (annotation != null) {
        return annotation.value();
      }
      cls = cls.getSuperclass();
    }
    // We don't know what this is supposed to be, return "UNK" for unknown...
    return "UNK";
  }

}
