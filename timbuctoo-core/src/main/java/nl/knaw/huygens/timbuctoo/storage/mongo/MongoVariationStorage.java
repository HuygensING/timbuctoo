package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class MongoVariationStorage extends MongoStorageBase implements VariationStorage {

  private static final Logger LOG = LoggerFactory.getLogger(MongoVariationStorage.class);

  private final ObjectMapper objectMapper;
  private final TreeEncoderFactory treeEncoderFactory;
  private final TreeDecoderFactory treeDecoderFactory;
  private final Map<Class<? extends Entity>, DBCollection> collectionCache;
  private final VariationInducer inducer;
  private final VariationReducer reducer;

  public MongoVariationStorage(TypeRegistry registry, Mongo mongo, DB db, String dbName, MongoObjectMapper mongoMapper, MongoFieldMapper mongoFieldMapper) throws UnknownHostException, MongoException {
    super(registry, mongo, db, dbName, mongoMapper);
    objectMapper = new ObjectMapper();
    treeEncoderFactory = new TreeEncoderFactory(objectMapper);
    treeDecoderFactory = new TreeDecoderFactory();
    collectionCache = Maps.newHashMap();
    inducer = new VariationInducer(registry, mongoMapper, mongoFieldMapper);
    reducer = new VariationReducer(registry, mongoMapper, mongoFieldMapper);
  }

  public void createIndexes() {
    DBCollection collection = db.getCollection("relation");
    collection.ensureIndex(new BasicDBObject("^sourceId", 1));
    collection.ensureIndex(new BasicDBObject("^targetId", 1));
    collection.ensureIndex(new BasicDBObject("^sourceId", 1).append("^targetId", 1));
  }

  private <T extends Entity> DBCollection getVariationCollection(Class<T> type) {
    DBCollection col = collectionCache.get(type);
    if (col == null) {
      Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
      col = db.getCollection(typeRegistry.getINameForType(baseType));
      col.setDBDecoderFactory(treeDecoderFactory);
      col.setDBEncoderFactory(treeEncoderFactory);
      collectionCache.put(type, col);
    }
    return col;
  }

  private <T extends Entity> DBCollection getRawVersionCollection(Class<T> type) {
    Class<? extends Entity> baseType = typeRegistry.getBaseClass(type);
    DBCollection col = db.getCollection(MongoUtils.getVersioningCollectionName(baseType));
    col.setDBDecoderFactory(treeDecoderFactory);
    col.setDBEncoderFactory(treeEncoderFactory);
    return col;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Entity> T getItem(Class<T> type, String id) throws VariationException, IOException {
    DBCollection col = getVariationCollection(type);
    DBObject query = new BasicDBObject("_id", id);
    return (T) reducer.reduceDBObject((Class<? extends DomainEntity>) type, col.findOne(query));
  }

  @Override
  public <T extends DomainEntity> List<T> getAllVariations(Class<T> type, String id) throws VariationException, IOException {
    DBObject query = new BasicDBObject("_id", id);
    DBObject item = getVariationCollection(type).findOne(query);
    List<T> variations = reducer.getAllForDBObject(item, type);
    for (T variation : variations) {
      addRelationsTo(variation.getClass(), id, variation);
    }
    return variations;
  }

  @Override
  public <T extends DomainEntity> T getVariation(Class<T> type, String id, String variation) throws IOException {
    DBObject query = new BasicDBObject("_id", id);
    DBObject item = getVariationCollection(type).findOne(query);
    return reducer.reduceDBObject(item, type, variation);
  }

  @Override
  public <T extends Entity> StorageIterator<T> getAllByType(Class<T> cls) {
    DBCollection col = getVariationCollection(cls);
    return new MongoDBVariationIterator<T>(col.find(), reducer, cls);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Entity> MongoChanges<T> getAllRevisions(Class<T> type, String id) throws IOException {
    DBObject query = new BasicDBObject("_id", id);
    DBObject allRevisions = getRawVersionCollection(type).findOne(query);
    return (MongoChanges<T>) reducer.reduceMultipleRevisions((Class<? extends DomainEntity>) type, allRevisions);
  }

  @Override
  public <T extends DomainEntity> T getRevision(Class<T> type, String id, int revisionId) throws IOException {
    DBObject query = new BasicDBObject("_id", id);
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
    DBCollection col = getVariationCollection(type);
    JacksonDBObject<JsonNode> insertedItem = new JacksonDBObject<JsonNode>(jsonNode, JsonNode.class);
    col.insert(insertedItem);
    addInitialVersion(type, item.getId(), insertedItem);
    return item.getId();
  }

  @Override
  public <T extends Entity> void updateItem(Class<T> type, String id, T item) throws IOException {
    DBCollection col = getVariationCollection(type);
    BasicDBObject q = new BasicDBObject("_id", id);
    q.put("^rev", item.getRev());
    DBObject existingNode = col.findOne(q);
    if (existingNode == null) {
      throw new IOException("No entity was found for ID " + id + " and revision " + String.valueOf(item.getRev()) + " !");
    }
    JsonNode updatedNode = inducer.induce(type, item, existingNode);
    ((ObjectNode) updatedNode).put("^rev", item.getRev() + 1);
    JacksonDBObject<JsonNode> updatedDBObj = new JacksonDBObject<JsonNode>(updatedNode, JsonNode.class);
    col.update(q, updatedDBObj);
    addVersion(type, id, updatedDBObj);
  }

  @Override
  public <T extends DomainEntity> void deleteItem(Class<T> type, String id, Change change) throws IOException {
    DBCollection col = getVariationCollection(type);
    BasicDBObject q = new BasicDBObject("_id", id);
    DBObject existingNode = col.findOne(q);
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
    node.put("^pid", (String) null);
    JsonNode changeTree = objectMapper.valueToTree(change);
    node.put("^lastChange", changeTree);
    int rev = node.get("^rev").asInt();
    node.put("^rev", rev + 1);
    q.put("^rev", rev);
    JacksonDBObject<JsonNode> updatedNode = new JacksonDBObject<JsonNode>(node, JsonNode.class);
    col.update(q, updatedNode);
    addVersion(type, id, updatedNode);
  }

  private <T extends Entity> void addInitialVersion(Class<T> cls, String id, JacksonDBObject<JsonNode> initialVersion) {
    DBCollection col = getRawVersionCollection(cls);
    JsonNode actualVersion = initialVersion.getObject();

    ArrayNode versionsNode = objectMapper.createArrayNode();
    versionsNode.add(actualVersion);

    ObjectNode itemNode = objectMapper.createObjectNode();
    itemNode.put("versions", versionsNode);
    itemNode.put("_id", id);

    col.insert(new JacksonDBObject<JsonNode>(itemNode, JsonNode.class));
  }

  private <T extends Entity> void addVersion(Class<T> cls, String id, JacksonDBObject<JsonNode> newVersion) {
    DBCollection col = getRawVersionCollection(cls);
    JsonNode actualVersion = newVersion.getObject();

    ObjectNode versionNode = objectMapper.createObjectNode();
    versionNode.put("versions", actualVersion);

    ObjectNode update = objectMapper.createObjectNode();
    update.put("$push", versionNode);

    col.update(new BasicDBObject("_id", id), new JacksonDBObject<JsonNode>(update, JsonNode.class));
  }

  @Override
  public boolean relationExists(Relation relation) {
    DBCollection col = db.getCollection("relation");
    BasicDBObject query = new BasicDBObject();
    query.append("^typeId", relation.getTypeId());
    query.append("^sourceId", relation.getSourceId());
    query.append("^targetId", relation.getTargetId());
    return (col.count(query) != 0);
  }

  @Override
  public StorageIterator<Relation> getRelationsOf(Class<? extends DomainEntity> type, String id) throws IOException {
    DBObject query = DBQuery.or(DBQuery.is("^sourceId", id), DBQuery.is("^targetId", id));
    DBCursor cursor = getVariationCollection(Relation.class).find(query);
    return new MongoDBVariationIterator<Relation>(cursor, reducer, Relation.class);
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
      LOG.error("Error while handling {} {}", type.getName(), id);
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
  public <T extends Entity> void setPID(Class<T> cls, String id, String pid) {
    BasicDBObject query = new BasicDBObject("_id", id);
    BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("^pid", pid));
    getVariationCollection(cls).update(query, update);
  }

  @Override
  public <T extends DomainEntity> List<String> getAllIdsWithoutPIDOfType(Class<T> type) throws IOException {
    List<String> list = Lists.newArrayList();

    try {
      String variationName = reducer.typeToVariationName(type);
      DBObject query = new BasicDBObject(variationName, new BasicDBObject("$ne", null));
      query.put("^pid", null);
      DBObject columnsToShow = new BasicDBObject("_id", 1);

      DBCursor cursor = getVariationCollection(type).find(query, columnsToShow);
      while (cursor.hasNext()) {
        list.add((String) cursor.next().get("_id"));
      }
    } catch (MongoException e) {
      LOG.error("Error while retrieving objects without pid of type {}", type);
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
      query.put("^pid", null);
      getVariationCollection(type).remove(query);
    } catch (MongoException e) {
      LOG.error("Error while removing entities of type '{}'", type);
      throw new IOException(e);
    }
  }

}
