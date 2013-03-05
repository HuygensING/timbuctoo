package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.storage.RevisionChanges;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.generic.GenericDBRef;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.mongo.MongoModifiableStorage;

@Singleton
public class MongoComplexStorage implements Storage {
  
  private String dbName;
  private MongoOptions options;
  private Mongo mongo;
  private DB db;
  private MongoModifiableStorage plainStorage;
  private MongoModifiableVariationStorage variationStorage;
  
  private Set<String> variationDoctypes;
  private final DocumentTypeRegister docTypeRegistry;

  @Inject
  public MongoComplexStorage(StorageConfiguration conf, DocumentTypeRegister docTypeRegistry) throws UnknownHostException, MongoException {
    this.docTypeRegistry = docTypeRegistry;
    dbName = conf.getDbName();
    options = new MongoOptions();
    options.safe = true;
    mongo = new Mongo(new ServerAddress(conf.getHost(), conf.getPort()), options);
    db = mongo.getDB(dbName);
    if (conf.requiresAuth()) {
      db.authenticate(conf.getUser(), conf.getPassword().toCharArray());
    }
    variationDoctypes = conf.getVariationDocumentTypes();
    plainStorage = new MongoModifiableStorage(conf, mongo, db, docTypeRegistry);
    variationStorage = new MongoModifiableVariationStorage(conf, mongo, db, options, docTypeRegistry);
  }
  
  private Storage getStorageForType(Class<? extends Document > cls) {
    if (variationDoctypes.contains(docTypeRegistry.getCollectionId(cls))) {
      return variationStorage;
    }
    return plainStorage;
  }

  @Override
  public <T extends Document> T getItem(String id, Class<T> cls) throws IOException {
    return getStorageForType(cls).getItem(id, cls);
  }

  @Override
  public <T extends Document> StorageIterator<T> getAllByType(Class<T> cls) {
    return getStorageForType(cls).getAllByType(cls);
  }

  @Override
  public <T extends Document> StorageIterator<T> getByMultipleIds(Collection<String> ids, Class<T> entityCls) {
    return getStorageForType(entityCls).getAllByType(entityCls);
  }

  @Override
  public <T extends Document> void addItem(T newItem, Class<T> cls) throws IOException {
    getStorageForType(cls).addItem(newItem, cls);
  }

  @Override
  public <T extends Document> void addItems(List<T> items, Class<T> cls) throws IOException {
    getStorageForType(cls).addItems(items, cls);
  }

  @Override
  public <T extends Document> void updateItem(String id, T updatedItem, Class<T> cls) throws IOException {
    getStorageForType(cls).updateItem(id, updatedItem, cls);
  }

  @Override
  public <T extends Document> void deleteItem(String id, Class<T> cls, Change change) throws IOException {
    getStorageForType(cls).deleteItem(id, cls, change);
  }

  @Override
  public <T extends Document> RevisionChanges<T> getAllRevisions(String id, Class<T> baseCls) {
    return getStorageForType(baseCls).getAllRevisions(id, baseCls);
  }

  @Override
  public void destroy() {
    db.cleanCursors(true);
    mongo.close();
    System.err.println("Stopped Mongo.");
  }

  @Override
  public List<Document> getLastChanged(int limit) throws IOException {
    return variationStorage.getLastChanged(limit);
  }

  @Override
  public void empty() {
    db.cleanCursors(true);
    mongo.dropDatabase(dbName);
    db = mongo.getDB(dbName);
    plainStorage.resetDB(db);
    variationStorage.resetDB(db);
  }

  @Override
  public <T extends Document> void fetchAll(List<GenericDBRef<T>> refs, Class<T> cls) {
    getStorageForType(cls).fetchAll(refs, cls);
  }

  @Override
  public <T extends Document> List<String> getIdsForQuery(Class<T> cls, List<String> accessors, String[] id) {
    return getStorageForType(cls).getIdsForQuery(cls, accessors, id);
  }

  @Override
  public void ensureIndex(Class<? extends Document> cls, List<List<String>> accessorList) {
    getStorageForType(cls).ensureIndex(cls, accessorList);
  }

}
