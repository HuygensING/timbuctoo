package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.BasicStorage;
import nl.knaw.huygens.timbuctoo.storage.RevisionChanges;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.VariationStorage;

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

  private final Mongo mongo;
  private final String dbName;
  private DB db;
  private final MongoStorage plainStorage;
  private final MongoVariationStorage variationStorage;

  @Inject
  public MongoStorageFacade(TypeRegistry registry, Configuration config, MongoObjectMapper mongoMapper, MongoFieldMapper mongoFieldMapper) throws UnknownHostException, MongoException {
    MongoOptions options = new MongoOptions();
    options.safe = true;

    String host = config.getSetting("database.host", "localhost");
    int port = config.getIntSetting("database.port", 27017);
    mongo = new Mongo(new ServerAddress(host, port), options);

    dbName = config.getSetting("database.name");
    db = mongo.getDB(dbName);

    String user = config.getSetting("database.user");
    if (!user.isEmpty()) {
      String password = config.getSetting("database.password");
      db.authenticate(user, password.toCharArray());
    }

    plainStorage = new MongoStorage(registry, mongo, db, dbName, mongoMapper);
    variationStorage = new MongoVariationStorage(registry, mongo, db, dbName, mongoMapper, mongoFieldMapper);
    variationStorage.createIndexes();
  }

  @Override
  public void empty() {
    db.cleanCursors(true);
    mongo.dropDatabase(dbName);
    db = mongo.getDB(dbName);
    plainStorage.resetDB(db);
    variationStorage.resetDB(db);
    variationStorage.createIndexes();
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
  public <T extends Entity> T getItem(Class<T> type, String id) throws IOException {
    return getStorageFor(type).getItem(type, id);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getAllByType(Class<T> type) {
    return getStorageFor(type).getAllByType(type);
  }

  @Override
  public <T extends Entity> long count(Class<T> type) {
    return getStorageFor(type).count(type);
  }

  @Override
  public <T extends SystemEntity> T findItemByKey(Class<T> type, String key, String value) throws IOException {
    return getStorageFor(type).findItemByKey(type, key, value);
  }

  @Override
  public <T extends SystemEntity> T findItem(Class<T> type, T item) throws IOException {
    return getStorageFor(type).findItem(type, item);
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
  public <T extends Entity> void deleteItem(Class<T> type, String id, Change change) throws IOException {
    getStorageFor(type).deleteItem(type, id, change);
  }

  @Override
  public <T extends Entity> RevisionChanges<T> getAllRevisions(Class<T> type, String id) throws IOException {
    return getStorageFor(type).getAllRevisions(type, id);
  }

  @Override
  public <T extends SystemEntity> void removeItem(Class<T> type, String id) throws IOException {
    getStorageFor(type).removeItem(type, id);
  }

  @Override
  public <T extends SystemEntity> int removeAll(Class<T> type) {
    return getStorageFor(type).removeAll(type);
  }

  @Override
  public <T extends SystemEntity> int removeByDate(Class<T> type, String dateField, Date dateValue) {
    return getStorageFor(type).removeByDate(type, dateField, dateValue);
  }

  // -------------------------------------------------------------------

  @Override
  public <T extends DomainEntity> T getVariation(Class<T> type, String id, String variation) throws IOException {
    return variationStorage.getVariation(type, id, variation);
  }

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws IOException {
    return variationStorage.getAllVariations(type, id);
  }

  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws IOException {
    throw new UnsupportedOperationException("Method not available for this type");
  }

  @Override
  public boolean relationExists(Relation relation) {
    return variationStorage.relationExists(relation);
  }

  @Override
  public StorageIterator<Relation> getRelationsOf(Class<? extends DomainEntity> type, String id) throws IOException {
    return variationStorage.getRelationsOf(type, id);
  }

  @Override
  public void addRelationsTo(Class<? extends DomainEntity> type, String id, DomainEntity entity) {
    variationStorage.addRelationsTo(type, id, entity);
  }

  @Override
  public <T extends Entity> void setPID(Class<T> cls, String id, String pid) {
    variationStorage.setPID(cls, id, pid);
  }

  @Override
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws IOException {
    return variationStorage.getAllIdsWithoutPIDOfType(type);
  }

  @Override
  public List<String> getRelationIds(List<String> ids) throws IOException {
    return variationStorage.getRelationIds(ids);
  }

  @Override
  public <T extends DomainEntity> void removeNonPersistent(Class<T> type, List<String> ids) throws IOException {
    variationStorage.removeNonPersistent(type, ids);
  }

}
