package nl.knaw.huygens.repository.storage.mongo;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.generic.GenericDBRef;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.generic.StorageUtils;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

@Singleton
public abstract class MongoStorage implements Storage {

  protected Mongo mongo;
  protected DB db;
  protected String dbName;

  private final String COUNTER_COLLECTION_NAME = "counters";
  protected JacksonDBCollection<Counter, String> counterCol;

  private Set<String> documentCollections;
  protected Set<String> versionedDocumentTypes;
  private final DocumentTypeRegister docTypeRegistry;

  static class Counter {
    @JsonProperty("_id")
    public String id;
    public int next;
  }

  @Inject
  public MongoStorage(StorageConfiguration conf, DocumentTypeRegister docTypeRegistry) throws UnknownHostException, MongoException {
    this.docTypeRegistry = docTypeRegistry;
    dbName = conf.getDbName();
    mongo = new Mongo(conf.getHost(), conf.getPort());
    db = mongo.getDB(dbName);
    if (conf.requiresAuth()) {
      db.authenticate(conf.getUser(), conf.getPassword().toCharArray());
    }
    db.setWriteConcern(WriteConcern.SAFE);
    initializeDB(conf);
  }

  private void initializeDB(StorageConfiguration conf) {
    counterCol = JacksonDBCollection.wrap(db.getCollection(COUNTER_COLLECTION_NAME), Counter.class, String.class);
    documentCollections = conf.getDocumentTypes();
    versionedDocumentTypes = conf.getVersionedTypes();
  }

  public MongoStorage(StorageConfiguration conf, Mongo m, DB loanedDB, DocumentTypeRegister docTypeRegistry) {
    mongo = m;
    db = loanedDB;
    this.docTypeRegistry = docTypeRegistry;
    initializeDB(conf);
  }

  @Override
  public <T extends Document> T getItem(Class<T> type, String id) {
    JacksonDBCollection<T, String> col = MongoUtils.getCollection(db, type);
    return col.findOneById(id);
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
  public void destroy() {
    db.cleanCursors(true);
    mongo.close();
    System.err.println("Stopped Mongo.");
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
      JacksonDBCollection<? extends Document, String> col = MongoUtils.getCollection(db, docTypeRegistry.getClassFromTypeString(colName));
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

}
