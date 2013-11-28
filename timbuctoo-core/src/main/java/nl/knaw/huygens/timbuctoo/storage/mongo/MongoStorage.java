package nl.knaw.huygens.timbuctoo.storage.mongo;

import static com.google.common.base.Preconditions.checkState;
import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.EmptyStorageIterator;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import org.mongojack.DBQuery;
import org.mongojack.internal.stream.JacksonDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;

public class MongoStorage implements Storage {

  private static final Logger LOG = LoggerFactory.getLogger(MongoStorage.class);

  private final TypeRegistry typeRegistry;
  private final Mongo mongo;
  private final DB db;
  private final EntityIds entityIds;

  private MongoQueries queries;
  private ObjectMapper objectMapper;
  private TreeEncoderFactory treeEncoderFactory;
  private TreeDecoderFactory treeDecoderFactory;
  private VariationInducer inducer;
  private VariationReducer reducer;

  @Inject
  public MongoStorage(TypeRegistry registry, Configuration config) throws UnknownHostException, MongoException {
    typeRegistry = registry;
    MongoOptions options = new MongoOptions();
    options.safe = true;

    String host = config.getSetting("database.host", "localhost");
    int port = config.getIntSetting("database.port", 27017);
    mongo = new Mongo(new ServerAddress(host, port), options);

    String dbName = config.getSetting("database.name");
    db = mongo.getDB(dbName);

    String user = config.getSetting("database.user");
    if (!user.isEmpty()) {
      String password = config.getSetting("database.password");
      db.authenticate(user, password.toCharArray());
    }

    entityIds = new EntityIds(db, typeRegistry);

    initialize();
    ensureIndexes();
  }

  @VisibleForTesting
  MongoStorage(TypeRegistry registry, Mongo mongo, DB db, EntityIds entityIds) {
    this.typeRegistry = registry;
    this.mongo = mongo;
    this.db = db;
    this.entityIds = entityIds;

    initialize();
  }

  private void initialize() {
    queries = new MongoQueries();
    objectMapper = new ObjectMapper();
    treeEncoderFactory = new TreeEncoderFactory(objectMapper);
    treeDecoderFactory = new TreeDecoderFactory();
    inducer = new VariationInducer(typeRegistry);
    reducer = new VariationReducer(typeRegistry);
  }

  private void ensureIndexes() {
    DBCollection collection = db.getCollection("relation");
    collection.ensureIndex(new BasicDBObject("^sourceId", 1));
    collection.ensureIndex(new BasicDBObject("^targetId", 1));
    collection.ensureIndex(new BasicDBObject("^sourceId", 1).append("^targetId", 1));
  }

  @Override
  public void close() {
    db.cleanCursors(true);
    mongo.close();
    LOG.info("Closed");
  }

  // --- support -------------------------------------------------------

  private final Map<Class<? extends Entity>, DBCollection> collectionCache = Maps.newHashMap();

  private <T extends Entity> DBCollection getDBCollection(Class<T> type) {
    DBCollection collection = collectionCache.get(type);
    if (collection == null) {
      Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
      String collectionName = typeRegistry.getINameForType(baseType);
      checkState(collectionName != null, "Unregistered type %s", type.getSimpleName());
      collection = db.getCollection(collectionName);
      collection.setDBDecoderFactory(treeDecoderFactory);
      collection.setDBEncoderFactory(treeEncoderFactory);
      collectionCache.put(type, collection);
      LOG.info("Added {} to collection cache", type.getSimpleName());
    }
    return collection;
  }

  private <T extends Entity> DBCollection getVersionCollection(Class<T> type) {
    Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
    DBCollection col = db.getCollection(getVersioningCollectionName(baseType));
    col.setDBDecoderFactory(treeDecoderFactory);
    col.setDBEncoderFactory(treeEncoderFactory);
    return col;
  }

  private String getCollectionName(Class<? extends Entity> type) {
    return type.getSimpleName().toLowerCase();
  }

  private String getVersioningCollectionName(Class<? extends Entity> type) {
    return getCollectionName(type) + "_versions";
  }

  /**
   * Sets the id of the specified entity to the next value
   * for the collection in which the entity is stored.
   */
  private <T extends Entity> void setNextId(Class<T> type, T entity) {
    entity.setId(entityIds.getNextId(type));
  }

  @SuppressWarnings("unchecked")
  private JsonNode toJsonNode(DBObject object) throws IOException {
    if (object instanceof JacksonDBObject) {
      return (((JacksonDBObject<JsonNode>) object).getObject());
    } else if (object instanceof DBJsonNode) {
      return ((DBJsonNode) object).getDelegate();
    } else {
      LOG.error("Failed to convert {}", object.getClass());
      throw new IOException("Unknown DBObject type");
    }
  }

  // --- generic storage layer -----------------------------------------

  private <T extends Entity> T getItem(Class<T> type, DBObject query) throws IOException {
    DBObject item = getDBCollection(type).findOne(query);
    return (item != null) ? reducer.reduceVariation(type, toJsonNode(item), null) : null;
  }

  private <T extends Entity> StorageIterator<T> getItems(Class<T> type, DBObject query) {
    DBCursor cursor = getDBCollection(type).find(query);
    return (cursor != null) ? new MongoStorageIterator<T>(type, cursor, reducer) : new EmptyStorageIterator<T>();
  }

  private <T extends Entity> int removeItem(Class<T> type, DBObject query) {
    WriteResult result = getDBCollection(type).remove(query);
    return (result != null) ? result.getN() : 0;
  }

  public <T extends Entity> long count(Class<T> type) {
    Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
    return getDBCollection(baseType).count();
  }

  // --- entities ------------------------------------------------------

  @Override
  public <T extends Entity> T getItem(Class<T> type, String id) throws IOException {
    DBObject query = queries.selectById(id);
    return getItem(type, query);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getAllByType(Class<T> type) {
    DBObject query = queries.selectAll();
    return getItems(type, query);
  }

  @Override
  public <T extends Entity> String addItem(Class<T> type, T entity) throws IOException {
    if (entity.getId() == null) {
      setNextId(type, entity);
    }
    if (TypeRegistry.isDomainEntity(type)) {
      // administrative properties must be controlled in the storage layer
      DomainEntity domainEntity = DomainEntity.class.cast(entity);
      domainEntity.setVariations(null); // make sure the list is empty
      domainEntity.addVariation(getInternalName(typeRegistry.getBaseClass(type)));
      domainEntity.addVariation(getInternalName(type));
    }
    JsonNode jsonNode = inducer.induceNewEntity(type, entity);
    JacksonDBObject<JsonNode> insertedItem = new JacksonDBObject<JsonNode>(jsonNode, JsonNode.class);
    getDBCollection(type).insert(insertedItem);
    if (TypeRegistry.isDomainEntity(type)) {
      addInitialVersion(type, entity.getId(), insertedItem);
    }
    return entity.getId();
  }

  @Override
  public <T extends Entity> void updateItem(Class<T> type, String id, T entity) throws IOException {
    int revision = entity.getRev();
    DBObject query = queries.selectByIdAndRevision(id, revision);
    DBObject existingNode = getDBCollection(type).findOne(query);
    if (existingNode == null) {
      throw new IOException("No entity with id " + id + " and revision " + revision);
    }
    JsonNode updatedNode = inducer.induceOldEntity(type, entity, toJsonNode(existingNode));
    ((ObjectNode) updatedNode).put("^rev", revision + 1);
    JacksonDBObject<JsonNode> updatedDBObj = new JacksonDBObject<JsonNode>(updatedNode, JsonNode.class);
    getDBCollection(type).update(query, updatedDBObj);
    if (TypeRegistry.isDomainEntity(type)) {
      addVersion(type, id, updatedDBObj);
    }
  }

  // --- system entities -----------------------------------------------

  @Override
  public <T extends SystemEntity> T findItemByKey(Class<T> type, String key, String value) throws IOException {
    DBObject query = queries.selectByProperty(key, value);
    return getItem(type, query);
  }

  @Override
  public <T extends SystemEntity> T findItem(Class<T> type, T example) throws IOException {
    DBObject query = queries.selectByProperties(type, example);
    return getItem(type, query);
  }

  @Override
  public <T extends SystemEntity> int removeItem(Class<T> type, String id) {
    DBObject query = queries.selectById(id);
    return removeItem(type, query);
  }

  @Override
  public <T extends SystemEntity> int removeAll(Class<T> type) {
    DBObject query = queries.selectAll();
    return removeItem(type, query);
  }

  @Override
  public <T extends SystemEntity> int removeByDate(Class<T> type, String dateField, Date dateValue) {
    DBObject query = queries.selectByDate(dateField, dateValue);
    return removeItem(type, query);
  }

  private RelationType getRelationType(String id) throws IOException {
    DBObject query = queries.selectById(id);
    return getItem(RelationType.class, query);
  }

  // --- domain entities -----------------------------------------------

  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) {
    DBObject query = queries.selectById(id);
    DBObject update = queries.setProperty(DomainEntity.PID, pid);
    getDBCollection(type).update(query, update);
  }

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws StorageException, IOException {
    DBObject query = queries.selectById(id);
    DBObject item = getDBCollection(type).findOne(query);
    if (item == null) {
      return null;
    }
    List<T> variations = reducer.reduceAllVariations(type, toJsonNode(item));
    for (T variation : variations) {
      addRelationsTo(variation.getClass(), id, variation);
    }
    return variations;
  }

  @Override
  public <T extends DomainEntity> T getVariation(Class<T> type, String id, String variation) throws IOException {
    DBObject query = queries.selectById(id);
    DBObject item = getDBCollection(type).findOne(query);
    return (item != null) ? reducer.reduceVariation(type, toJsonNode(item), variation) : null;
  }

  @Override
  public <T extends DomainEntity> MongoChanges<T> getAllRevisions(Class<T> type, String id) throws IOException {
    DBObject query = queries.selectById(id);
    DBObject item = getVersionCollection(type).findOne(query);
    return (item != null) ? reducer.reduceAllRevisions(type, toJsonNode(item)) : null;
  }

  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws IOException {
    DBObject query = queries.selectById(id);
    query.put("versions.^rev", revisionId);
    DBObject item = getVersionCollection(type).findOne(query);
    return (item != null) ? reducer.reduceRevision(type, toJsonNode(item)) : null;
  }

  @Override
  public <T extends DomainEntity> void deleteItem(Class<T> type, String id, Change change) throws IOException {
    DBObject query = queries.selectById(id);
    DBObject existingNode = getDBCollection(type).findOne(query);
    if (existingNode == null) {
      throw new IOException("No entity was found for ID " + id);
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
    node.put(DomainEntity.DELETED, true);
    node.put(DomainEntity.PID, (String) null);
    JsonNode changeTree = objectMapper.valueToTree(change);
    node.put("^lastChange", changeTree);
    int rev = node.get("^rev").asInt();
    node.put("^rev", rev + 1);
    query.put("^rev", rev);
    JacksonDBObject<JsonNode> updatedNode = new JacksonDBObject<JsonNode>(node, JsonNode.class);
    getDBCollection(type).update(query, updatedNode);
    addVersion(type, id, updatedNode);
  }

  private <T extends Entity> void addInitialVersion(Class<T> type, String id, JacksonDBObject<JsonNode> initialVersion) {
    JsonNode actualVersion = initialVersion.getObject();

    ArrayNode versionsNode = objectMapper.createArrayNode();
    versionsNode.add(actualVersion);

    ObjectNode itemNode = objectMapper.createObjectNode();
    itemNode.put("versions", versionsNode);
    itemNode.put("_id", id);

    getVersionCollection(type).insert(new JacksonDBObject<JsonNode>(itemNode, JsonNode.class));
  }

  private <T extends Entity> void addVersion(Class<T> type, String id, JacksonDBObject<JsonNode> newVersion) {
    JsonNode actualVersion = newVersion.getObject();

    ObjectNode versionNode = objectMapper.createObjectNode();
    versionNode.put("versions", actualVersion);

    ObjectNode update = objectMapper.createObjectNode();
    update.put("$push", versionNode);
    DBObject updateObj = new JacksonDBObject<JsonNode>(update, JsonNode.class);

    getVersionCollection(type).update(new BasicDBObject("_id", id), updateObj);
  }

  @Override
  public boolean relationExists(Relation relation) throws IOException {
    DBObject query = queries.selectRelation(relation);
    return getItem(Relation.class, query) != null;
  }

  @Override
  public StorageIterator<Relation> getRelationsOf(Class<? extends DomainEntity> type, String id) throws IOException {
    DBObject query = DBQuery.or(DBQuery.is("^sourceId", id), DBQuery.is("^targetId", id));
    return getItems(Relation.class, query);
  }

  // We retrieve all relations involving the specified entity by its id.
  // Next we need to filter the relations that are compatible with the entity type:
  // a relation is only valid if the entity type we are handling is assignable
  // to the type specified in the relation.
  // For example, if a relation is specified for a DCARArchiver, it is visible when
  // dealing with an entity type DCARArchiver, but not for Archiver.
  //TODO add tests.
  @Override
  public void addRelationsTo(Class<? extends DomainEntity> type, String id, DomainEntity entity) {
    Preconditions.checkNotNull(entity, "entity cannot be null");
    StorageIterator<Relation> iterator = null;
    try {
      iterator = getRelationsOf(type, id); // db access
      while (iterator.hasNext()) {
        Relation relation = iterator.next(); // db access
        RelationType relType = getRelationType(relation.getTypeRef().getId());
        Preconditions.checkNotNull(relType, "Failed to retrieve relation type");
        if (relation.hasSourceId(id)) {
          Class<? extends Entity> cls = typeRegistry.getTypeForIName(relation.getSourceType());
          if (cls != null && cls.isAssignableFrom(type)) {
            Reference reference = relation.getTargetRef();
            entity.addRelation(relType.getRegularName(), getEntityRef(reference)); // db access
          }
        } else if (relation.hasTargetId(id)) {
          Class<? extends Entity> cls = typeRegistry.getTypeForIName(relation.getTargetType());
          if (cls != null && cls.isAssignableFrom(type)) {
            Reference reference = relation.getSourceRef();
            entity.addRelation(relType.getInverseName(), getEntityRef(reference)); // db access
          }
        } else {
          throw new IllegalStateException("Impossible");
        }
      }
    } catch (IOException e) {
      LOG.error("Error while handling {} {}", type.getSimpleName(), id);
    } finally {
      if (iterator != null) {
        iterator.close();
      }
    }
  }

  private EntityRef getEntityRef(Reference reference) throws StorageException, IOException {
    String iname = reference.getType();
    String xname = typeRegistry.getXNameForIName(iname);
    Class<? extends Entity> type = typeRegistry.getTypeForIName(iname);
    Entity entity = getItem(type, reference.getId());

    return new EntityRef(iname, xname, reference.getId(), entity.getDisplayName());
  }

  @Override
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws IOException {
    List<String> list = Lists.newArrayList();

    try {
      String variationName = typeRegistry.getIName(type);
      DBObject query = queries.selectVariation(variationName);
      query.put(DomainEntity.PID, new BasicDBObject("$exists", false));
      DBObject columnsToShow = new BasicDBObject("_id", 1);

      DBCursor cursor = getDBCollection(type).find(query, columnsToShow);
      while (cursor.hasNext()) {
        list.add((String) cursor.next().get("_id"));
      }

    } catch (MongoException e) {
      LOG.error("Error while retrieving objects without pid of type {}", type.getSimpleName());
      throw new IOException(e);
    }

    return list;
  }

  @Override
  public List<String> getRelationIds(List<String> ids) throws IOException {
    List<String> relationIds = Lists.newArrayList();

    try {
      DBObject query = DBQuery.or(DBQuery.in("^sourceId", ids), DBQuery.in("^targetId", ids));
      DBObject columnsToShow = new BasicDBObject("_id", 1);

      DBCursor cursor = db.getCollection("relation").find(query, columnsToShow);
      while (cursor.hasNext()) {
        relationIds.add((String) cursor.next().get("_id"));
      }
    } catch (MongoException e) {
      LOG.error("Error while retrieving relation id's of {}", ids);
      throw new IOException(e);
    }

    return relationIds;
  }

  @Override
  public <T extends DomainEntity> void removeNonPersistent(Class<T> type, List<String> ids) throws IOException {
    try {
      DBObject query = DBQuery.in("_id", ids);
      query.put(DomainEntity.PID, null);
      getDBCollection(type).remove(query);
    } catch (MongoException e) {
      LOG.error("Error while removing entities of type {}", type.getSimpleName());
      throw new IOException(e);
    }
  }

}
