package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.storage.RevisionChanges;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.generic.GenericDBRef;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.mongo.MongoModifiableStorage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

@Singleton
public class MongoStorageFacade implements nl.knaw.huygens.repository.storage.Storage {

  private String dbName;
  private MongoOptions options;
  private Mongo mongo;
  private DB db;
  private MongoModifiableStorage plainStorage;
  private MongoModifiableVariationStorage variationStorage;

  private Set<String> variationDoctypes;
  private final DocTypeRegistry docTypeRegistry;

  @Inject
  public MongoStorageFacade(StorageConfiguration conf, DocTypeRegistry docTypeRegistry) throws UnknownHostException, MongoException {
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

  private nl.knaw.huygens.repository.storage.mongo.MongoStorage getStorageForType(Class<? extends Document> cls) {
    if (variationDoctypes.contains(docTypeRegistry.getCollectionId(cls))) {
      return variationStorage;
    }
    return plainStorage;
  }

  @Override
  public <T extends Document> T getItem(Class<T> type, String id) throws IOException {
    return getStorageForType(type).getItem(type, id);
  }

  @Override
  public <T extends DomainDocument> T getVariation(Class<T> type, String id, String variation) throws IOException {
    return variationStorage.getVariation(type, id, variation);
  }

  @Override
  public <T extends Document> List<T> getAllVariations(Class<T> type, String id) throws IOException {
    if (variationDoctypes.contains(docTypeRegistry.getCollectionId(type))) {
      return variationStorage.getAllVariations(type, id);
    }
    throw new UnsupportedOperationException("Method not available for this type");
  }

  @Override
  public <T extends Document> StorageIterator<T> getAllByType(Class<T> cls) {
    return getStorageForType(cls).getAllByType(cls);
  }

  @Override
  public <T extends Document> StorageIterator<T> getByMultipleIds(Class<T> type, Collection<String> ids) {
    return getStorageForType(type).getAllByType(type);
  }

  @Override
  public <T extends Document> void addItem(Class<T> type, T item) throws IOException {
    getStorageForType(type).addItem(type, item);
  }

  @Override
  public <T extends Document> void addItems(Class<T> type, List<T> items) throws IOException {
    getStorageForType(type).addItems(type, items);
  }

  @Override
  public <T extends Document> void updateItem(Class<T> type, String id, T item) throws IOException {
    getStorageForType(type).updateItem(type, id, item);
  }

  @Override
  public <T extends Document> void setPID(Class<T> cls, String pid, String id) {
    getStorageForType(cls).setPID(cls, pid, id);
  }

  @Override
  public <T extends Document> void deleteItem(Class<T> type, String id, Change change) throws IOException {
    getStorageForType(type).deleteItem(type, id, change);
  }

  @Override
  public <T extends Document> RevisionChanges<T> getAllRevisions(Class<T> type, String id) {
    return getStorageForType(type).getAllRevisions(type, id);
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
  public <T extends Document> void fetchAll(Class<T> type, List<GenericDBRef<T>> refs) {
    getStorageForType(type).fetchAll(type, refs);
  }

  @Override
  public <T extends Document> List<String> getIdsForQuery(Class<T> type, List<String> accessors, String[] id) {
    return getStorageForType(type).getIdsForQuery(type, accessors, id);
  }

  @Override
  public <T extends Document> void ensureIndex(Class<T> type, List<List<String>> accessorList) {
    getStorageForType(type).ensureIndex(type, accessorList);
  }

}
