package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.model.util.IDPrefix;
import nl.knaw.huygens.repository.storage.generic.JsonViews;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.storage.mongo.MongoUtils;
import nl.knaw.huygens.repository.variation.VariationInducer;
import nl.knaw.huygens.repository.variation.VariationUtils;

@Singleton
public class MongoModifiableVariationStorage extends MongoVariationStorage {

  private VariationInducer inducer;

  @Inject
  public MongoModifiableVariationStorage(StorageConfiguration conf) throws UnknownHostException, MongoException {
    super(conf);
    inducer = new VariationInducer();
    inducer.setView(JsonViews.DBView.class);
  }
  
  public MongoModifiableVariationStorage(StorageConfiguration conf, Mongo m, DB db, MongoOptions options) throws UnknownHostException, MongoException {
    super(conf, m, db, options);
    inducer = new VariationInducer();
    inducer.setView(JsonViews.DBView.class);
  }

  @Override
  public <T extends Document> void addItem(T newItem, Class<T> cls) throws IOException {
    if (newItem.getId() == null) {
      setNextId(cls, newItem);
    }
    JsonNode jsonNode = inducer.induce(newItem, cls);
    DBCollection col = getVariationCollection(cls);
    JacksonDBObject<JsonNode> insertedItem = new JacksonDBObject<JsonNode>(jsonNode, JsonNode.class);
    col.insert(insertedItem);
    addInitialVersion(cls, newItem.getId(), insertedItem);
  }

  @Override
  public <T extends Document> void addItems(List<T> items, Class<T> cls) throws IOException {
    for (T item : items) {
      if (item.getId() == null) {
        setNextId(cls, item);
      }
    }
    List<JsonNode> jsonNodes = inducer.induce(items, cls, Collections.<String, DBObject>emptyMap());
    DBCollection col = getVariationCollection(cls);
    @SuppressWarnings("unchecked")
    JacksonDBObject<JsonNode>[] dbObjects = new JacksonDBObject[jsonNodes.size()];
    int i = 0;
    for (JsonNode n : jsonNodes) {
      JacksonDBObject<JsonNode> updatedDBObj = new JacksonDBObject<JsonNode>(n, JsonNode.class);
      dbObjects[i++] = updatedDBObj;
    }
    col.insert(dbObjects);
    for (i = 0; i < dbObjects.length; i++) {
      Object idObj = dbObjects[i].get("_id");
      String id = "";
      try {
        id = (String) idObj;
      } catch (Exception ex) {
        throw new IOException("Couldn't find an ID for this item: " + dbObjects[i].toString());
      }
      addInitialVersion(cls, id, dbObjects[i]);
    }
  }

  @Override
  public <T extends Document> void updateItem(String id, T updatedItem, Class<T> cls) throws IOException {
    DBCollection col = getVariationCollection(cls);
    BasicDBObject q = new BasicDBObject("_id", id);
    q.put("^rev", updatedItem.getRev());
    DBObject existingNode = col.findOne(q);
    if (existingNode == null) {
      throw new IOException("No document was found for ID " + id + " and revision " + String.valueOf(updatedItem.getRev()) + " !");
    }
    JsonNode updatedNode = inducer.induce(updatedItem, cls, existingNode);
    ((ObjectNode) updatedNode).put("^rev", updatedItem.getRev() + 1);
    JacksonDBObject<JsonNode> updatedDBObj = new JacksonDBObject<JsonNode>(updatedNode, JsonNode.class);
    col.update(q, updatedDBObj);
    addVersion(cls, id, updatedDBObj);
  }

  @Override
  public <T extends Document> void deleteItem(String id, Class<T> cls, Change change) throws IOException {
    DBCollection col = getVariationCollection(cls);
    BasicDBObject q = new BasicDBObject("_id", id);
    DBObject existingNode = col.findOne(q);
    if (existingNode == null) {
      throw new IOException("No document was found for ID " + id + "!");
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
    JsonNode changeTree = getMapper().valueToTree(change);
    node.put("^deleted", true).put("^lastChange", changeTree);
    int rev = node.get("^rev").asInt();
    node.put("^rev", rev + 1);
    q.put("^rev", rev);
    JacksonDBObject<JsonNode> updatedNode = new JacksonDBObject<JsonNode>(node, JsonNode.class);
    col.update(q, updatedNode);
    addVersion(cls, id, updatedNode);
  }

  private ObjectMapper getMapper() {
    return treeEncoderFactory.getObjectMapper();
  }
  
  private <T extends Document> void addInitialVersion(Class<T> cls, String id, JacksonDBObject<JsonNode> initialVersion) {
    DBCollection col = getRawVersionCollection(cls);
    JsonNode actualVersion = initialVersion.getObject();
    
    ObjectMapper mapper = getMapper();
    ArrayNode versionsNode = mapper.createArrayNode();
    versionsNode.add(actualVersion);
    
    ObjectNode itemNode = mapper.createObjectNode();
    itemNode.put("versions", versionsNode);
    itemNode.put("_id", id);
    
    col.insert(new JacksonDBObject<JsonNode>(itemNode, JsonNode.class));
  }
  
  private <T extends Document> void addVersion(Class<T> cls, String id, JacksonDBObject<JsonNode> newVersion) {
    DBCollection col = getRawVersionCollection(cls);
    JsonNode actualVersion = newVersion.getObject();
    
    ObjectMapper mapper = getMapper();
    ObjectNode versionNode = mapper.createObjectNode();
    versionNode.put("versions", actualVersion);
    
    ObjectNode update = mapper.createObjectNode();
    update.put("$push", versionNode);
    
    col.update(new BasicDBObject("_id", id), new JacksonDBObject<JsonNode>(update, JsonNode.class));
  }
  
  private <T extends Document> void setNextId(Class<T> cls, T item) {
    BasicDBObject idFinder = new BasicDBObject("_id", MongoUtils.getCollectionName(VariationUtils.getBaseClass(cls)));
    BasicDBObject counterIncrement = new BasicDBObject("$inc", new BasicDBObject("next", 1));

    // Find by id, return all fields, use default sort, increment the counter,
    // return the new object, create if no object exists:
    Counter newCounter = counterCol.findAndModify(idFinder, null, null, false, counterIncrement, true, true);

    String newId = cls.getAnnotation(IDPrefix.class).value() + String.format("%1$010d", newCounter.next);
    item.setId(newId);
  }

  @Override
  public void empty() {
    db.cleanCursors(true);
    mongo.dropDatabase(dbName);
    db = mongo.getDB(dbName);
  }
  
  public void resetDB(DB db) {
    this.db = db;
  }
}
