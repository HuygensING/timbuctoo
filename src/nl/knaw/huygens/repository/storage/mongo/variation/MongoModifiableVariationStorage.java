package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;

import net.vz.mongodb.jackson.internal.stream.JacksonDBObject;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.Change;
import nl.knaw.huygens.repository.storage.generic.JsonViews;
import nl.knaw.huygens.repository.storage.generic.StorageConfiguration;
import nl.knaw.huygens.repository.variation.VariationInducer;

public class MongoModifiableVariationStorage extends MongoVariationStorage {

  private VariationInducer inducer;

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
    JsonNode jsonNode = inducer.induce(newItem, cls);
    DBCollection col = getRawCollection(cls);
    JacksonDBObject<JsonNode> insertedItem = new JacksonDBObject<JsonNode>(jsonNode, JsonNode.class);
    col.insert(insertedItem);
    addInitialVersion(cls, newItem.getId(), insertedItem);
  }

  @Override
  public <T extends Document> void addItems(List<T> items, Class<T> cls) throws IOException {
    List<JsonNode> jsonNodes = inducer.induce(items, cls, Collections.<String, DBObject>emptyMap());
    DBCollection col = getRawCollection(cls);
    DBObject[] dbObjects = new DBObject[jsonNodes.size()];
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
    DBCollection col = getRawCollection(cls);
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
    DBCollection col = getRawCollection(cls);
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
    JsonNode changeTree = ((TreeEncoderFactory) options.dbEncoderFactory).getObjectMapper().valueToTree(change);
    node.put("^deleted", true).put("^lastChange", changeTree);
    int rev = node.get("^rev").asInt();
    node.put("^rev", rev + 1);
    q.put("^rev", rev);
    JacksonDBObject<JsonNode> updatedNode = new JacksonDBObject<JsonNode>(node, JsonNode.class);
    col.update(q, updatedNode);
    addVersion(cls, id, updatedNode);
  }
  
  private <T extends Document> void addInitialVersion(Class<T> cls, String id, DBObject initialVersion) {
    DBCollection col = getRawVersionCollection(cls);
    DBObject item = new BasicDBObject("_id", id);
    BasicDBList versionList = new BasicDBList();
    versionList.add(initialVersion);
    item.put("versions", versionList);
    col.insert(item);
  }
  
  private <T extends Document> void addVersion(Class<T> cls, String id, DBObject newVersion) {
    DBCollection col = getRawVersionCollection(cls);
    DBObject update = new BasicDBObject("$push", new BasicDBObject("versions", newVersion));
    col.update(new BasicDBObject("_id", id), update);
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

  @Override
  public <T extends Document> void removeReference(Class<T> cls, List<String> accessorList, List<String> referringIds, String referredId, Change change) {
    // TODO Auto-generated method stub
    
  }
}
