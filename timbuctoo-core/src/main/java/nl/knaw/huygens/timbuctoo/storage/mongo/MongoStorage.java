package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static com.google.common.base.Preconditions.checkState;
import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;
import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toBaseDomainEntity;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.EntityMapper;
import nl.knaw.huygens.timbuctoo.config.EntityMappers;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.Language;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationEntityRef;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.EmptyStorageIterator;
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;

import org.apache.commons.lang.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.internal.stream.JacksonDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
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

public class MongoStorage implements Storage {

  private static final Logger LOG = LoggerFactory.getLogger(MongoStorage.class);

  private final TypeRegistry typeRegistry;
  private final MongoDB mongoDB;
  private final EntityIds entityIds;

  private MongoQueries queries;
  private ObjectMapper objectMapper;
  private TreeEncoderFactory treeEncoderFactory;
  private TreeDecoderFactory treeDecoderFactory;
  private EntityInducer inducer;
  private EntityReducer reducer;
  private EntityMappers entityMappers;

  @Inject
  public MongoStorage(TypeRegistry registry, Configuration config) throws UnknownHostException, MongoException {
    typeRegistry = registry;
    MongoOptions options = new MongoOptions();
    options.safe = true;

    String host = config.getSetting("database.host", "localhost");
    int port = config.getIntSetting("database.port", 27017);
    Mongo mongo = new Mongo(new ServerAddress(host, port), options);

    String dbName = config.getSetting("database.name");
    DB db = mongo.getDB(dbName);

    String user = config.getSetting("database.user");
    if (!user.isEmpty()) {
      String password = config.getSetting("database.password");
      db.authenticate(user, password.toCharArray());
    }

    mongoDB = new MongoDB(mongo, db);
    entityIds = new EntityIds(db, typeRegistry);

    initialize();
    ensureIndexes();
  }

  @VisibleForTesting
  MongoStorage(TypeRegistry registry, Mongo mongo, DB db, EntityIds entityIds) {
    this.typeRegistry = registry;
    this.mongoDB = new MongoDB(mongo, db);
    this.entityIds = entityIds;

    initialize();
  }

  @VisibleForTesting
  MongoStorage(TypeRegistry registry, MongoDB mongoDB, EntityIds entityIds) {
    this.typeRegistry = registry;
    this.mongoDB = mongoDB;
    this.entityIds = entityIds;

    initialize();
  }

  private void initialize() {
    queries = new MongoQueries();
    objectMapper = new ObjectMapper();
    treeEncoderFactory = new TreeEncoderFactory(objectMapper);
    treeDecoderFactory = new TreeDecoderFactory();
    inducer = new EntityInducer();
    reducer = new EntityReducer(typeRegistry);
    entityMappers = new EntityMappers(typeRegistry.getDomainEntityTypes());
  }

  private void ensureIndexes() {
    DBCollection collection = getDBCollection(Relation.class);
    collection.ensureIndex(new BasicDBObject("^sourceId", 1));
    collection.ensureIndex(new BasicDBObject("^targetId", 1));
    collection.ensureIndex(new BasicDBObject("^sourceId", 1).append("^targetId", 1));

    collection = getDBCollection(Language.class);
    collection.ensureIndex(new BasicDBObject("^code", 1), new BasicDBObject("unique", true));
  }

  @Override
  public void close() {
    mongoDB.close();
  }

  // --- support -------------------------------------------------------

  private final Map<Class<? extends Entity>, DBCollection> collectionCache = Maps.newHashMap();

  private <T extends Entity> DBCollection getDBCollection(Class<T> type) {
    DBCollection collection = collectionCache.get(type);
    if (collection == null) {
      Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
      String collectionName = getInternalName(baseType);
      collection = mongoDB.getCollection(collectionName);
      collection.setDBDecoderFactory(treeDecoderFactory);
      collection.setDBEncoderFactory(treeEncoderFactory);
      collectionCache.put(type, collection);
    }
    return collection;
  }

  private <T extends Entity> DBCollection getVersionCollection(Class<T> type) {
    Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
    String collectionName = getInternalName(baseType) + "_versions";
    DBCollection collection = mongoDB.getCollection(collectionName);
    collection.setDBDecoderFactory(treeDecoderFactory);
    collection.setDBEncoderFactory(treeEncoderFactory);
    return collection;
  }

  private DBObject toDBObject(JsonNode node) {
    return new JacksonDBObject<JsonNode>(node, JsonNode.class);
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

  private JsonNode getExisting(Class<? extends Entity> type, DBObject query) throws IOException {
    DBObject dbObject = getDBCollection(type).findOne(query);
    if (dbObject == null) {
      LOG.error("No match for query {}", query);
      throw new IOException("No match");
    }
    return toJsonNode(dbObject);
  }

  private <T extends Entity> T getItem(Class<T> type, DBObject query) throws IOException {
    DBObject item = getDBCollection(type).findOne(query);
    return (item != null) ? reducer.reduceVariation(type, toJsonNode(item)) : null;
  }

  private <T extends Entity> StorageIterator<T> getItems(Class<T> type, DBObject query) {
    DBCursor cursor = getDBCollection(type).find(query);
    return (cursor != null) ? new MongoStorageIterator<T>(type, cursor, reducer) : new EmptyStorageIterator<T>();
  }

  // Should be the replacement of getItems
  private <T extends Entity> StorageIterator<T> findItems(Class<T> type, DBObject query) {
    DBCursor cursor = mongoDB.find(getDBCollection(type), query);
    return (cursor != null) ? new MongoStorageIterator<T>(type, cursor, reducer) : new EmptyStorageIterator<T>();
  }

  @Override
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
  public <T extends Entity> StorageIterator<T> getAllByIds(Class<T> type, List<String> ids) {
    return findItems(type, DBQuery.in(Entity.ID, ids));
  }

  @Override
  public <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws IOException {
    Change change = Change.newInternalInstance();
    String id = entityIds.getNextId(type);

    entity.setId(id);
    entity.setRev(1);
    entity.setCreated(change);
    entity.setModified(change);

    JsonNode tree = inducer.induceSystemEntity(type, entity);
    mongoDB.insert(getDBCollection(type), id, toDBObject(tree));

    return id;
  }

  @Override
  public <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws IOException {
    String id = entityIds.getNextId(type);

    entity.setId(id);
    entity.setRev(1);
    entity.setCreated(change);
    entity.setModified(change);

    entity.setPid(null);
    entity.setDeleted(false);
    entity.setVariations(null); // make sure the list is empty
    entity.addVariation(typeRegistry.getBaseClass(type));
    entity.addVariation(type);

    JsonNode tree = inducer.induceDomainEntity(type, entity);
    mongoDB.insert(getDBCollection(type), id, toDBObject(tree));

    return id;
  }

  @Override
  public <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws IOException {
    Change change = Change.newInternalInstance();
    String id = entity.getId();
    int revision = entity.getRev();
    DBObject query = queries.selectByIdAndRevision(id, revision);

    JsonNode tree = getExisting(type, query);
    SystemEntity systemEntity = reducer.reduceVariation(type, tree);

    systemEntity.setRev(revision + 1);
    systemEntity.setModified(change);

    inducer.adminSystemEntity(systemEntity, (ObjectNode) tree);
    inducer.induceSystemEntity(type, entity, (ObjectNode) tree);

    mongoDB.update(getDBCollection(type), query, toDBObject(tree));
  }

  @Override
  public <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws IOException {
    String id = entity.getId();
    int revision = entity.getRev();
    DBObject query = queries.selectByIdAndRevision(id, revision);

    JsonNode tree = getExisting(type, query);
    DomainEntity domainEntity = reducer.reduceVariation(toBaseDomainEntity(type), tree);

    domainEntity.setRev(revision + 1);
    domainEntity.setModified(change);
    domainEntity.setPid(null);
    domainEntity.addVariation(type);

    inducer.adminDomainEntity(domainEntity, (ObjectNode) tree);
    inducer.induceDomainEntity(type, entity, (ObjectNode) tree);

    mongoDB.update(getDBCollection(type), query, toDBObject(tree));
  }

  @Override
  public <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id, Change change) throws IOException {
    DBObject query = queries.selectById(id);

    JsonNode tree = getExisting(type, query);
    DomainEntity entity = reducer.reduceVariation(toBaseDomainEntity(type), tree);
    int revision = entity.getRev();

    entity.setRev(revision + 1);
    entity.setModified(change);
    entity.setPid(null);
    entity.setDeleted(true);
    entity.setVariations(null);

    inducer.adminDomainEntity(entity, (ObjectNode) tree);
    // TODO remove "real" data

    mongoDB.update(getDBCollection(type), query, toDBObject(tree));
  }

  @Override
  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws IOException {
    DBObject query = queries.selectById(id);

    JsonNode tree = getExisting(type, query);
    DomainEntity domainEntity = reducer.reduceVariation(toBaseDomainEntity(type), tree);

    if (!StringUtils.isBlank(domainEntity.getPid())) {
      throw new IllegalStateException(String.format("%s with %s already has a pid: %s", type.getSimpleName(), id, pid));
    }

    domainEntity.setPid(pid);

    inducer.adminDomainEntity(domainEntity, (ObjectNode) tree);

    mongoDB.update(getDBCollection(type), query, toDBObject(tree));

    addVersion(type, id, tree);
  }

  private <T extends Entity> void addVersion(Class<T> type, String id, JsonNode tree) throws IOException {
    DBCollection collection = getVersionCollection(type);
    DBObject query = queries.selectById(id);

    if (collection.findOne(query) == null) {
      ObjectNode node = objectMapper.createObjectNode();
      node.put("_id", id);
      node.put("versions", objectMapper.createArrayNode());
      mongoDB.insert(collection, id, toDBObject(node));
    }

    ObjectNode versionNode = objectMapper.createObjectNode();
    versionNode.put("versions", tree);
    ObjectNode update = objectMapper.createObjectNode();
    update.put("$push", versionNode);
    mongoDB.update(collection, query, toDBObject(update));
  }

  // --- system entities -----------------------------------------------

  @Override
  public <T extends Entity> T findItemByKey(Class<T> type, String key, String value) throws IOException {
    DBObject query = queries.selectByProperty(key, value);
    return getItem(type, query);
  }

  @Override
  public <T extends Entity> T findItem(Class<T> type, T example) throws IOException {
    DBObject query = queries.selectByProperties(type, example);
    return getItem(type, query);
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws IOException {
    return mongoDB.remove(getDBCollection(type), queries.selectById(id));
  }

  @Override
  public <T extends SystemEntity> int deleteAll(Class<T> type) throws IOException {
    return mongoDB.remove(getDBCollection(type), queries.selectAll());
  }

  @Override
  public <T extends SystemEntity> int deleteByDate(Class<T> type, String dateField, Date dateValue) throws IOException {
    return mongoDB.remove(getDBCollection(type), queries.selectByDate(type, dateField, dateValue));
  }

  private RelationType getRelationType(String id) throws IOException {
    DBObject query = queries.selectById(id);
    return getItem(RelationType.class, query);
  }

  // --- domain entities -----------------------------------------------

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws IOException {
    DBObject query = queries.selectById(id);
    DBObject item = getDBCollection(type).findOne(query);
    if (item != null) {
      return reducer.reduceAllVariations(type, toJsonNode(item));
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public <T extends DomainEntity> MongoChanges<T> getAllRevisions(Class<T> type, String id) throws IOException {
    DBObject query = queries.selectById(id);
    DBObject item = getVersionCollection(type).findOne(query);
    return (item != null) ? reducer.reduceAllRevisions(type, toJsonNode(item)) : null;
  }

  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revision) throws IOException {
    DBObject query = queries.selectById(id);
    DBObject projection = queries.getRevisionProjection(revision);
    DBObject dbObject = getVersionCollection(type).findOne(query, projection);
    return (dbObject != null) ? reducer.reduceVariation(type, toJsonNode(dbObject)) : null;
  }

  @Override
  public boolean relationExists(Relation relation) throws IOException {
    DBObject query = queries.selectRelation(relation);
    return getItem(Relation.class, query) != null;
  }

  @Override
  public <T extends DomainEntity> void addRelationsTo(T entity) {
    if (entity != null) {
      String id = entity.getId();
      Class<? extends DomainEntity> type = entity.getClass();
      EntityMapper mapper = entityMappers.getEntityMapper(type);
      checkState(mapper != null, "No EntityMapper for type %s", type);
      @SuppressWarnings("unchecked")
      Class<? extends Relation> mappedType = (Class<? extends Relation>) mapper.map(Relation.class);
      StorageIterator<? extends Relation> iterator = null;
      try {
        DBObject query = DBQuery.or(DBQuery.is("^sourceId", id), DBQuery.is("^targetId", id));
        iterator = getItems(mappedType, query); // db access
        while (iterator.hasNext()) {
          Relation relation = iterator.next(); // db access
          RelationType relType = getRelationType(relation.getTypeRef().getId());
          checkState(relType != null, "Failed to retrieve relation type");
          if (relation.hasSource() && relation.hasTarget()) {
            if (relation.hasSourceId(id)) {
              EntityRef entityRef = getEntityRef(mapper, relation.getTargetRef(), relation.getId(), relation.isAccepted(), relation.getRev());
              entity.addRelation(relType.getRegularName(), entityRef); // db access
            } else if (relation.hasTargetId(id)) {
              EntityRef entityRef = getEntityRef(mapper, relation.getSourceRef(), relation.getId(), relation.isAccepted(), relation.getRev());
              entity.addRelation(relType.getInverseName(), entityRef); // db access
            }
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
  }

  // Relations are defined between primitive domain entities
  // Map to a domain entity in the package from which an entity is requested
  private EntityRef getEntityRef(EntityMapper mapper, Reference reference, String relationId, boolean accepted, int rev) throws StorageException, IOException {
    String iname = reference.getType();
    Class<? extends DomainEntity> type = TypeRegistry.toDomainEntity(typeRegistry.getTypeForIName(iname));
    type = mapper.map(type);
    iname = TypeNames.getInternalName(type);
    String xname = typeRegistry.getXNameForIName(iname);
    DomainEntity entity = getItem(type, reference.getId());

    return new RelationEntityRef(iname, xname, reference.getId(), entity.getDisplayName(), relationId, accepted, rev);
  }

  @Override
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws IOException {
    List<String> list = Lists.newArrayList();

    try {
      String variationName = typeRegistry.getINameForType(type);
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

      DBCursor cursor = getDBCollection(Relation.class).find(query, columnsToShow);
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
  public <T extends DomainEntity> void deleteNonPersistent(Class<T> type, List<String> ids) throws IOException {
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
