package nl.knaw.huygens.timbuctoo.storage.mongo;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import static nl.knaw.huygens.timbuctoo.config.TypeNames.getInternalName;
import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.toBaseDomainEntity;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.EntityInducer;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;
import nl.knaw.huygens.timbuctoo.storage.NoSuchEntityException;
import nl.knaw.huygens.timbuctoo.storage.Properties;
import nl.knaw.huygens.timbuctoo.storage.Storage;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.storage.UpdateException;

import org.apache.commons.lang.StringUtils;
import org.mongojack.internal.stream.JacksonDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class MongoStorage implements Storage {

  private static final Logger LOG = LoggerFactory.getLogger(MongoStorage.class);

  private final MongoDB mongoDB;
  private final EntityIds entityIds;
  private final EntityInducer inducer;
  private final EntityReducer reducer;
  private final MongoQueries queries;
  private final ObjectMapper objectMapper;
  private final Properties properties;
  private final TreeEncoderFactory treeEncoderFactory;
  private final TreeDecoderFactory treeDecoderFactory;

  @Inject
  public MongoStorage(MongoDB mongoDB, EntityIds entityIds, Properties properties, EntityInducer inducer, EntityReducer reducer) {
    this.mongoDB = mongoDB;
    this.entityIds = entityIds;
    this.properties = properties;
    this.inducer = inducer;
    this.reducer = reducer;

    queries = new MongoQueries();
    objectMapper = new ObjectMapper();
    treeEncoderFactory = new TreeEncoderFactory(objectMapper);
    treeDecoderFactory = new TreeDecoderFactory();
  }

  private String propertyName(Class<? extends Entity> type, String fieldName) {
    return properties.propertyName(type, fieldName);
  }

  @Override
  public void createIndex(boolean unique, Class<? extends Entity> type, String... fields) throws StorageException {
    DBObject keys = new BasicDBObject();
    for (String field : fields) {
      keys.put(propertyName(type, field), 1);
    }
    mongoDB.createIndex(getDBCollection(type), keys, new BasicDBObject("unique", unique));
  }

  @Override
  public <T extends Entity> String getStatistics(Class<T> type) {
    try {
      return mongoDB.getStats(getDBCollection(type)).toString();
    } catch (StorageException e) {
      return "?";
    }
  }

  @Override
  public void close() {
    mongoDB.close();
  }

  @Override
  public boolean isAvailable() {
    return mongoDB.isAvailable();
  }

  // --- support -------------------------------------------------------

  private final Map<Class<? extends Entity>, DBCollection> collectionCache = Maps.newHashMap();

  private <T extends Entity> DBCollection getDBCollection(Class<T> type) {
    DBCollection collection = collectionCache.get(type);
    if (collection == null) {
      Class<? extends Entity> baseType = getBaseClass(type);
      String collectionName = getInternalName(baseType);
      collection = mongoDB.getCollection(collectionName);
      collection.setDBDecoderFactory(treeDecoderFactory);
      collection.setDBEncoderFactory(treeEncoderFactory);
      collectionCache.put(type, collection);
    }
    return collection;
  }

  private <T extends Entity> DBCollection getVersionCollection(Class<T> type) {
    Class<? extends Entity> baseType = getBaseClass(type);
    String collectionName = getInternalName(baseType) + "_versions";
    DBCollection collection = mongoDB.getCollection(collectionName);
    collection.setDBDecoderFactory(treeDecoderFactory);
    collection.setDBEncoderFactory(treeEncoderFactory);
    return collection;
  }

  private Class<? extends Entity> getBaseClass(Class<? extends Entity> type) {
    return TypeRegistry.getBaseClass(type);
  }

  private DBObject toDBObject(JsonNode node) {
    return new JacksonDBObject<JsonNode>(node, JsonNode.class);
  }

  @SuppressWarnings("unchecked")
  private JsonNode toJsonNode(DBObject object) throws StorageException {
    if (object instanceof JacksonDBObject) {
      return (((JacksonDBObject<JsonNode>) object).getObject());
    } else if (object instanceof DBJsonNode) {
      return ((DBJsonNode) object).getDelegate();
    } else {
      LOG.error("Failed to convert {}", object.getClass());
      throw new StorageException("Unknown DBObject type");
    }
  }

  @VisibleForTesting
  static <T extends Entity> StorageIterator<T> newStorageIterator(Class<T> type, DBCursor cursor, EntityReducer reducer) {
    if (cursor == null) {
      return StorageIteratorStub.newInstance();
    } else {
      return new MongoStorageIterator<T>(type, cursor, reducer);
    }
  }

  // --- generic storage layer -----------------------------------------

  private JsonNode getExisting(Class<? extends Entity> type, DBObject query) throws StorageException {
    DBObject dbObject = getDBCollection(type).findOne(query);
    if (dbObject == null) {
      LOG.error("No match for query \"%s\"", query);
      throw new NoSuchEntityException("No match for query", query);
    }
    return toJsonNode(dbObject);
  }

  private <T extends Entity> T getItem(Class<T> type, DBObject query) throws StorageException {
    DBObject item = mongoDB.findOne(getDBCollection(type), query);
    return (item != null) ? reducer.reduceVariation(type, toJsonNode(item)) : null;
  }

  private <T extends Entity> StorageIterator<T> findItems(Class<T> type, DBObject query) throws StorageException {
    DBCursor cursor = mongoDB.find(getDBCollection(type), query);
    return newStorageIterator(type, cursor, reducer);
  }

  @Override
  public <T extends Entity> long count(Class<T> type) {
    try {
      return mongoDB.count(getDBCollection(type));
    } catch (StorageException e) {
      return 0;
    }
  }

  // --- entities ------------------------------------------------------

  @Override
  public <T extends Entity> boolean entityExists(Class<T> type, String id) throws StorageException {
    DBObject query = queries.selectById(id);
    return mongoDB.exist(getDBCollection(type), query);
  }

  @Override
  public <T extends Entity> T getEntityOrDefaultVariation(Class<T> type, String id) throws StorageException {
    DBObject query = queries.selectById(id);
    return getItem(type, query);
  }

  @Override
  public <T extends Entity> T getEntity(Class<T> type, String id) throws StorageException {
    DBObject query = queries.selectById(id);

    DBObject object = mongoDB.findOne(getDBCollection(type), query);

    if (object == null) {
      return null;
    }

    JsonNode existing = toJsonNode(object);

    if (TypeRegistry.isDomainEntity(type) && existing.isObject()) {
      ObjectNode objectTree = (ObjectNode) existing;
      if (!doesVariationExist(TypeNames.getInternalName(type), objectTree)) {
        return null;
      }
    }

    return reducer.reduceVariation(type, existing);
  }

  @Override
  public <T extends SystemEntity> StorageIterator<T> getSystemEntities(Class<T> type) throws StorageException {
    DBObject query = queries.selectAll();
    return findItems(type, query);
  }

  @Override
  public <T extends DomainEntity> StorageIterator<T> getDomainEntities(Class<T> type) throws StorageException {
    DBObject query = TypeRegistry.isPrimitiveDomainEntity(type) ? queries.selectAll() : queries.selectVariation(type);
    return findItems(type, query);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getEntitiesByProperty(Class<T> type, String field, String value) throws StorageException {
    DBObject query = queries.selectByProperty(propertyName(type, field), value);
    return findItems(type, query);
  }

  @Override
  public <T extends SystemEntity> String addSystemEntity(Class<T> type, T entity) throws StorageException {
    Change change = Change.newInternalInstance();
    String id = entityIds.getNextId(type);

    entity.setId(id);
    entity.setRev(1);
    entity.setCreated(change);
    entity.setModified(change);

    JsonNode tree = inducer.convertSystemEntityForAdd(type, entity);
    mongoDB.insert(getDBCollection(type), id, toDBObject(tree));

    return id;
  }

  @Override
  public <T extends DomainEntity> String addDomainEntity(Class<T> type, T entity, Change change) throws StorageException {
    String id = entityIds.getNextId(type);

    entity.setId(id);
    entity.setRev(1);
    entity.setCreated(change);
    entity.setModified(change);

    entity.setPid(null);
    entity.setDeleted(false);
    entity.setVariations(null); // make sure the list is empty
    entity.addVariation(toBaseDomainEntity(type));
    entity.addVariation(type);

    JsonNode tree = inducer.convertDomainEntityForAdd(type, entity);
    mongoDB.insert(getDBCollection(type), id, toDBObject(tree));

    return id;
  }

  @Override
  public <T extends SystemEntity> void updateSystemEntity(Class<T> type, T entity) throws UpdateException, StorageException {
    Change change = Change.newInternalInstance();
    String id = entity.getId();
    int revision = entity.getRev();

    checkIfEntityExists(type, id, revision);

    DBObject query = queries.selectByIdAndRevision(id, revision);

    JsonNode tree = getExisting(type, query);
    SystemEntity systemEntity = reducer.reduceVariation(type, tree);

    systemEntity.setRev(revision + 1);
    systemEntity.setModified(change);

    inducer.adminSystemEntity(systemEntity, (ObjectNode) tree);
    inducer.convertSystemEntityForUpdate(type, entity, (ObjectNode) tree);

    mongoDB.update(getDBCollection(type), query, toDBObject(tree));
  }

  @Override
  public <T extends DomainEntity> void updateDomainEntity(Class<T> type, T entity, Change change) throws UpdateException, StorageException {
    String id = entity.getId();
    int revision = entity.getRev();

    checkIfEntityExists(type, id, revision);

    DBObject query = queries.selectByIdAndRevision(id, revision);

    JsonNode tree = getExisting(type, query);
    DomainEntity domainEntity = reducer.reduceVariation(toBaseDomainEntity(type), tree);

    domainEntity.setRev(revision + 1);
    domainEntity.setModified(change);
    domainEntity.setPid(null);
    domainEntity.addVariation(type);

    inducer.adminDomainEntity(domainEntity, (ObjectNode) tree);
    inducer.convertDomainEntityForUpdate(type, entity, (ObjectNode) tree);

    mongoDB.update(getDBCollection(type), query, toDBObject(tree));
  }

  /**
   * A method that checks if the entity is present in the database.
   * @param type the type of the entity
   * @param id the id of the entity
   * @param revision the revision of the entity
   * @throws StorageException when one of the query executions fails.
   * @throws NoSuchEntityException when the entity is not available at all.
   * @throws UpdateException when the requested version is not available.
   */
  private <T extends Entity> void checkIfEntityExists(Class<T> type, String id, int revision) throws StorageException, NoSuchEntityException, UpdateException {
    if (!revisionExists(type, id, revision)) {
      if (!entityExists(type, id)) {
        throw new NoSuchEntityException("\"%s\" with id \"%s\" does not exist.", type.getSimpleName(), id);
      }
      throw new UpdateException(String.format("\"%s\" with id \"%s\" and revision \"%s\" could not be updated.", type.getSimpleName(), id, "" + revision));
    }
  }

  private <T extends Entity> boolean revisionExists(Class<T> type, String id, int revision) throws StorageException {
    DBObject query = queries.selectByIdAndRevision(id, revision);
    return mongoDB.exist(getDBCollection(type), query);
  }

  @Override
  public <T extends DomainEntity> void deleteDomainEntity(Class<T> type, String id, Change change) throws StorageException {
    if (!TypeRegistry.isPrimitiveDomainEntity(type)) {
      throw new IllegalArgumentException("Only primitive DomainEntities can be deleted. " + type.getSimpleName() + " is not a primitive DomainEntity.");
    }
    if (mongoDB.remove(getDBCollection(type), queries.selectById(id)) <= 0) {
      throw new NoSuchEntityException(type, id);
    }
  }

  @Override
  public void deleteRelationsOfEntity(Class<Relation> type, String id) throws StorageException {
    mongoDB.remove(getDBCollection(type), queries.selectRelationsByEntityId(id));
  }

  @Override
  public <T extends DomainEntity> void deleteVariation(Class<T> type, String id, Change change) throws StorageException {
    String variationToDelete = TypeNames.getInternalName(type);
    if (TypeRegistry.isPrimitiveDomainEntity(type)) {
      throwTypeIsAPrimitiveException(type);
    }

    DBObject query = queries.selectById(id);
    JsonNode tree = getExisting(type, query);

    if (tree.isObject()) {
      ObjectNode objectTree = (ObjectNode) tree;

      if (!doesVariationExist(variationToDelete, objectTree)) {
        throw new NoSuchEntityException(type, id);
      }

      List<String> fieldsToDelete = Lists.newArrayList();
      for (Iterator<String> fieldNames = objectTree.fieldNames(); fieldNames.hasNext();) {
        String fieldName = fieldNames.next();
        if (StringUtils.startsWith(fieldName, variationToDelete)) {
          fieldsToDelete.add(fieldName);
        }
      }
      objectTree.remove(fieldsToDelete);

    }

    DomainEntity entity = reducer.reduceVariation(toBaseDomainEntity(type), tree);
    int revision = entity.getRev();

    entity.setRev(revision + 1);
    entity.setModified(change);
    entity.setPid(null);
    entity.getVariations().remove(variationToDelete);

    inducer.adminDomainEntity(entity, (ObjectNode) tree);

    mongoDB.update(getDBCollection(type), query, toDBObject(tree));
  }

  @Override
  public void declineRelationsOfEntity(Class<? extends Relation> type, String id) throws StorageException {
    if (TypeRegistry.isPrimitiveDomainEntity(type)) {
      throwTypeIsAPrimitiveException(type);
    }

    Map<String, Object> propertyMap = Maps.newHashMap();
    propertyMap.put(propertyName(type, "accepted"), false);
    propertyMap.put(DomainEntity.PID, null);

    BasicDBObject setQuery = new BasicDBObject();
    setQuery.putAll(queries.setPropertiesToValue(propertyMap));
    setQuery.putAll(queries.incrementRevision());

    getDBCollection(type).update(queries.selectRelationsByEntityId(id), setQuery, false, true);
  }

  private void throwTypeIsAPrimitiveException(Class<? extends DomainEntity> type) throws IllegalArgumentException {
    throw new IllegalArgumentException("Only project variations can be deleted. " + type.getSimpleName() + " is a primitive.");
  }

  @Override
  public <T extends DomainEntity> void setPID(Class<T> type, String id, String pid) throws StorageException {
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

  private <T extends Entity> void addVersion(Class<T> type, String id, JsonNode tree) throws StorageException {
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
  public <T extends Entity> T findItemByProperty(Class<T> type, String field, String value) throws StorageException {
    DBObject query = queries.selectByProperty(propertyName(type, field), value);
    return getItem(type, query);
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntity(Class<T> type, String id) throws StorageException {
    DBObject query = queries.selectById(id);
    return mongoDB.remove(getDBCollection(type), query);
  }

  @Override
  public <T extends SystemEntity> int deleteSystemEntities(Class<T> type) throws StorageException {
    DBObject query = queries.selectAll();
    return mongoDB.remove(getDBCollection(type), query);
  }

  @Override
  public <T extends SystemEntity> int deleteByModifiedDate(Class<T> type, Date dateValue) throws StorageException {
    DBObject query = queries.selectByModifiedDate(dateValue);
    return mongoDB.remove(getDBCollection(type), query);
  }

  // --- domain entities -----------------------------------------------

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws StorageException {
    Preconditions.checkArgument(TypeRegistry.isPrimitiveDomainEntity(type), "Nonprimitive type %s", type);
    DBObject query = queries.selectById(id);
    DBObject item = mongoDB.findOne(getDBCollection(type), query);
    if (item != null) {
      return reducer.reduceAllVariations(type, toJsonNode(item));
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public <T extends DomainEntity> List<T> getAllRevisions(Class<T> type, String id) throws StorageException {
    DBObject query = queries.selectById(id);
    DBObject item = mongoDB.findOne(getVersionCollection(type), query);
    return (item != null) ? reducer.reduceAllRevisions(type, toJsonNode(item)) : Lists.<T> newArrayList();
  }

  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revision) throws StorageException {
    DBObject query = queries.getRevisionFromVersionCollection(id, revision);
    DBObject projection = queries.getRevisionProjection(revision);
    DBObject dbObject = getVersionCollection(type).findOne(query, projection);
    return (dbObject != null) ? reducer.reduceVariation(type, toJsonNode(dbObject)) : null;
  }

  @Override
  public <T extends Relation> StorageIterator<T> getRelationsByEntityId(Class<T> type, String id) throws StorageException {
    DBObject query = queries.selectRelationsByEntityId(id);
    return findItems(type, query);
  }

  @Override
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws StorageException {
    List<String> list = Lists.newArrayList();

    try {
      DBObject query = queries.selectVariationWithoutPID(type);
      DBObject columnsToShow = new BasicDBObject("_id", 1);

      DBCursor cursor = getDBCollection(type).find(query, columnsToShow);
      while (cursor.hasNext()) {
        list.add((String) cursor.next().get("_id"));
      }

    } catch (MongoException e) {
      LOG.error("Error while retrieving objects without pid of type {}", type.getSimpleName());
      throw new StorageException(e);
    }

    return list;
  }

  @Override
  public List<String> getRelationIds(List<String> ids) throws StorageException {
    List<String> relationIds = Lists.newArrayList();

    try {
      DBObject query = queries.selectRelationsByEntityIds(ids);
      DBObject columnsToShow = new BasicDBObject("_id", 1);

      DBCursor cursor = getDBCollection(Relation.class).find(query, columnsToShow);
      while (cursor.hasNext()) {
        relationIds.add((String) cursor.next().get("_id"));
      }
    } catch (MongoException e) {
      LOG.error("Error while retrieving relation id's of {}", ids);
      throw new StorageException(e);
    }

    return relationIds;
  }

  @Override
  public <T extends Relation> T findRelation(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    if (sourceId != null && targetId != null && relationTypeId != null) {
      DBObject query = new BasicDBObject();
      query.put(Relation.SOURCE_ID, sourceId);
      query.put(Relation.TARGET_ID, targetId);
      query.put(Relation.TYPE_ID, relationTypeId);
      return getItem(type, query);
    }
    return null;
  }

  @Override
  public <T extends Relation> StorageIterator<T> findRelations(Class<T> type, String sourceId, String targetId, String relationTypeId) throws StorageException {
    DBObject query = queries.selectRelationsByIds(sourceId, targetId, relationTypeId);
    return findItems(type, query);
  }

  @Override
  public <T extends Relation> List<T> getRelationsByType(Class<T> type, List<String> relationTypeIds) throws StorageException {
    DBObject query = queries.selectByProperty(propertyName(type, Relation.TYPE_ID), relationTypeIds);
    return findItems(type, query).getAll();
  }

  @Override
  public <T extends DomainEntity> void deleteNonPersistent(Class<T> type, List<String> ids) throws StorageException {
    DBObject query = queries.selectNonPersistent(ids);
    mongoDB.remove(getDBCollection(type), query);
  }

  @Override
  public boolean doesVariationExist(Class<? extends DomainEntity> type, String id) throws StorageException {
    try {
      JsonNode node = getExisting(type, queries.selectById(id));

      if (node.isObject()) {
        String variation = TypeNames.getInternalName(type);
        return doesVariationExist(variation, (ObjectNode) node);
      }

    } catch (NoSuchEntityException e) {
      return false;
    }

    return false;
  }

  private boolean doesVariationExist(String variation, ObjectNode objectTree) {
    ArrayNode variations = (ArrayNode) objectTree.get(DomainEntity.VARIATIONS);
    for (Iterator<JsonNode> variationIterator = variations.elements(); variationIterator.hasNext();) {
      if (Objects.equal(variation, variationIterator.next().asText())) {
        return true;
      }
    }
    return false;
  }

}
