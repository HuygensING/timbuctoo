package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.model.DomainEntity;
import nl.knaw.huygens.repository.model.Entity;
import nl.knaw.huygens.repository.model.Relation;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.storage.BasicStorage;
import nl.knaw.huygens.repository.storage.GenericDBRef;
import nl.knaw.huygens.repository.storage.RevisionChanges;
import nl.knaw.huygens.repository.storage.StorageConfiguration;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.VariationStorage;
import nl.knaw.huygens.repository.storage.mongo.MongoStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

/**
 * Delegates storage operation to plain storage or variation storage.
 * Variation storage is used for all domain entities.
 */
@Singleton
public class MongoStorageFacade implements VariationStorage {

  private static final Logger LOG = LoggerFactory.getLogger(MongoStorageFacade.class);

  private final String dbName;
  private final Mongo mongo;
  private DB db;
  private final MongoStorage plainStorage;
  private final MongoVariationStorage variationStorage;

  @Inject
  public MongoStorageFacade(DocTypeRegistry registry, StorageConfiguration conf) throws UnknownHostException, MongoException {
    dbName = conf.getDbName();
    MongoOptions options = new MongoOptions();
    options.safe = true;
    mongo = new Mongo(new ServerAddress(conf.getHost(), conf.getPort()), options);
    db = mongo.getDB(dbName);
    if (conf.requiresAuth()) {
      db.authenticate(conf.getUser(), conf.getPassword().toCharArray());
    }
    plainStorage = new MongoStorage(registry, conf, mongo, db);
    variationStorage = new MongoVariationStorage(registry, conf, mongo, db, options);
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
  public void close() {
    db.cleanCursors(true);
    mongo.close();
    LOG.info("Closed");
  }

  // -------------------------------------------------------------------

  private BasicStorage getStorageFor(Class<? extends Entity> type) {
    return DomainEntity.class.isAssignableFrom(type) ? variationStorage : plainStorage;
  }

  @Override
  public <T extends Entity> void ensureIndex(Class<T> type, List<List<String>> accessors) {
    getStorageFor(type).ensureIndex(type, accessors);
  }

  @Override
  public <T extends Entity> T getItem(Class<T> type, String id) throws IOException {
    return getStorageFor(type).getItem(type, id);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getAllByType(Class<T> type) {
    return getStorageFor(type).getAllByType(type);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getByMultipleIds(Class<T> type, Collection<String> ids) {
    return getStorageFor(type).getByMultipleIds(type, ids);
  }

  @Override
  public <T extends Entity> String addItem(Class<T> type, T item) throws IOException {
    return getStorageFor(type).addItem(type, item);
  }

  @Override
  public <T extends Entity> void updateItem(Class<T> type, String id, T item) throws IOException {
    getStorageFor(type).updateItem(type, id, item);
  }

  @Override
  public <T extends Entity> void setPID(Class<T> cls, String pid, String id) {
    getStorageFor(cls).setPID(cls, pid, id);
  }

  @Override
  public <T extends Entity> void deleteItem(Class<T> type, String id, Change change) throws IOException {
    getStorageFor(type).deleteItem(type, id, change);
  }

  @Override
  public <T extends Entity> RevisionChanges<T> getAllRevisions(Class<T> type, String id) throws IOException {
    return getStorageFor(type).getAllRevisions(type, id);
  }

  @Override
  public List<Entity> getLastChanged(int limit) throws IOException {
    return variationStorage.getLastChanged(limit);
  }

  @Override
  public <T extends Entity> void fetchAll(Class<T> type, List<GenericDBRef<T>> refs) {
    getStorageFor(type).fetchAll(type, refs);
  }

  @Override
  public <T extends Entity> List<String> getIdsForQuery(Class<T> type, List<String> accessors, String[] id) {
    return getStorageFor(type).getIdsForQuery(type, accessors, id);
  }

  @Override
  public <T extends Entity> T searchItem(Class<T> type, T item) throws IOException {
    return getStorageFor(type).searchItem(type, item);
  }

  @Override
  public <T extends Entity> int removeAll(Class<T> type) {
    return getStorageFor(type).removeAll(type);
  }

  @Override
  public <T extends Entity> int removeByDate(Class<T> type, String dateField, Date dateValue) {
    return getStorageFor(type).removeByDate(type, dateField, dateValue);
  }

  // -------------------------------------------------------------------

  @Override
  public <T extends DomainEntity> T getVariation(Class<T> type, String id, String variation) throws IOException {
    return variationStorage.getVariation(type, id, variation);
  }

  @Override
  public <T extends Entity> List<T> getAllVariations(Class<T> type, String id) throws IOException {
    if (DomainEntity.class.isAssignableFrom(type)) {
      return variationStorage.getAllVariations(type, id);
    }
    throw new UnsupportedOperationException("Method not available for this type");
  }

  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws IOException {
    throw new UnsupportedOperationException("Method not available for this type");
  }

  @Override
  public int countRelations(Relation relation) {
    return variationStorage.countRelations(relation);
  }

  @Override
  public Collection<String> getRelationIds(Collection<String> ids) throws IOException {
    return variationStorage.getRelationIds(ids);
  }

  public <T extends DomainEntity> Collection<String> getAllIdsWithoutPIDOfType(Class<T> type) throws IOException {
    return variationStorage.getAllIdsWithoutPIDOfType(type);
  }

  @Override
  public <T extends DomainEntity> void removePermanently(Class<T> type, Collection<String> ids) throws IOException {
    variationStorage.removePermanently(type, ids);
  }

}
