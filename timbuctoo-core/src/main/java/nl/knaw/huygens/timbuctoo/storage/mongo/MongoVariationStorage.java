package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.EntityRef;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.VariationStorage;

import org.mongojack.DBQuery;
import org.mongojack.internal.stream.JacksonDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoVariationStorage extends MongoStorageBase implements VariationStorage {

  private static final Logger LOG = LoggerFactory.getLogger(MongoVariationStorage.class);

  public MongoVariationStorage(TypeRegistry registry, Mongo mongo, DB db, String dbName) throws UnknownHostException, MongoException {
    super(registry, mongo, db, dbName);
  }

  public void createIndexes() {
    DBCollection collection = db.getCollection("relation");
    collection.ensureIndex(new BasicDBObject("^sourceId", 1));
    collection.ensureIndex(new BasicDBObject("^targetId", 1));
    collection.ensureIndex(new BasicDBObject("^sourceId", 1).append("^targetId", 1));
  }

  private <T extends Entity> DBCollection getRawVersionCollection(Class<T> type) {
    Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
    DBCollection col = db.getCollection(getVersioningCollectionName(baseType));
    col.setDBDecoderFactory(treeDecoderFactory);
    col.setDBEncoderFactory(treeEncoderFactory);
    return col;
  }

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws VariationException, IOException {
    DBObject query = queries.selectById(id);
    DBObject item = getDBCollection(type).findOne(query);
    List<T> variations = reducer.getAllForDBObject(item, type);
    for (T variation : variations) {
      addRelationsTo(variation.getClass(), id, variation);
    }
    return variations;
  }

  @Override
  public <T extends DomainEntity> T getVariation(Class<T> type, String id, String variation) throws IOException {
    DBObject query = queries.selectById(id);
    DBObject item = getDBCollection(type).findOne(query);
    return reducer.reduceDBObject(item, type, variation);
  }

  @Override
  public <T extends DomainEntity> MongoChanges<T> getAllRevisions(Class<T> type, String id) throws IOException {
    DBObject query = queries.selectById(id);
    DBObject allRevisions = getRawVersionCollection(type).findOne(query);
    return reducer.reduceMultipleRevisions(type, allRevisions);
  }

  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws IOException {
    DBObject query = queries.selectById(id);
    query.put("versions.^rev", revisionId);
    DBObject item = getRawVersionCollection(type).findOne(query);
    return reducer.reduceRevision(type, item);
  }

  // -------------------------------------------------------------------

  @Override
  public <T extends Entity> String addItem(Class<T> type, T item) throws IOException {
    if (item.getId() == null) {
      setNextId(type, item);
    }
    JsonNode jsonNode = inducer.induce(type, item);
    JacksonDBObject<JsonNode> insertedItem = new JacksonDBObject<JsonNode>(jsonNode, JsonNode.class);
    getDBCollection(type).insert(insertedItem);
    if (TypeRegistry.isDomainEntity(type)) {
      addInitialVersion(type, item.getId(), insertedItem);
    }
    return item.getId();
  }

  @Override
  public <T extends Entity> void updateItem(Class<T> type, String id, T item) throws IOException {
    DBObject query = queries.selectById(id);
    query.put("^rev", item.getRev());
    DBObject existingNode = getDBCollection(type).findOne(query);
    if (existingNode == null) {
      throw new IOException("No entity was found for ID " + id + " and revision " + String.valueOf(item.getRev()) + " !");
    }
    JsonNode updatedNode = inducer.induce(type, item, existingNode);
    ((ObjectNode) updatedNode).put("^rev", item.getRev() + 1);
    JacksonDBObject<JsonNode> updatedDBObj = new JacksonDBObject<JsonNode>(updatedNode, JsonNode.class);
    getDBCollection(type).update(query, updatedDBObj);
    if (TypeRegistry.isDomainEntity(type)) {
      addVersion(type, id, updatedDBObj);
    }
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
    node.put("^deleted", true);
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

    getRawVersionCollection(type).insert(new JacksonDBObject<JsonNode>(itemNode, JsonNode.class));
  }

  private <T extends Entity> void addVersion(Class<T> type, String id, JacksonDBObject<JsonNode> newVersion) {
    JsonNode actualVersion = newVersion.getObject();

    ObjectNode versionNode = objectMapper.createObjectNode();
    versionNode.put("versions", actualVersion);

    ObjectNode update = objectMapper.createObjectNode();
    update.put("$push", versionNode);
    DBObject updateObj = new JacksonDBObject<JsonNode>(update, JsonNode.class);

    getRawVersionCollection(type).update(new BasicDBObject("_id", id), updateObj);
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

  private EntityRef getEntityRef(Reference reference) throws VariationException, IOException {
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
      String variationName = reducer.typeToVariationName(type);
      DBObject query = queries.selectVariation(variationName);
      query.put(DomainEntity.PID, null);
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
