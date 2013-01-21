package nl.knaw.huygens.repository.storage.mongo.variation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

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

  @Override
  public <T extends Document> void addItem(T newItem, Class<T> cls) throws IOException {
    JsonNode jsonNode = inducer.induce(newItem, cls);
    DBCollection col = getRawCollection(cls);
    JacksonDBObject<JsonNode> insertedItem = new JacksonDBObject<JsonNode>();
    insertedItem.setObject(jsonNode);
    col.insert(insertedItem);
  }

  @Override
  public <T extends Document> void addItems(List<T> items, Class<T> cls) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public <T extends Document> void updateItem(String id, T updatedItem, Class<T> cls) throws IOException {
    DBCollection col = getRawCollection(cls);
    BasicDBObject q = new BasicDBObject("_id", id);
    q.put("^rev", updatedItem.getRev());
    DBObject existingNode = col.findOne(q);
    JsonNode updatedNode = inducer.induce(updatedItem, cls, existingNode);
    ((ObjectNode) updatedNode).put("^rev", updatedItem.getRev() + 1);
    JacksonDBObject<JsonNode> updatedDBObj = new JacksonDBObject<JsonNode>();
    updatedDBObj.setObject(updatedNode);
    col.update(q, updatedDBObj);
  }

  @Override
  public <T extends Document> void deleteItem(String id, Class<T> cls, Change change) throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void empty() {
    db.cleanCursors(true);
    mongo.dropDatabase(dbName);
    db = mongo.getDB(dbName);
  }

  @Override
  public <T extends Document> void removeReference(Class<T> cls, List<String> accessorList, List<String> referringIds, String referredId, Change change) {
    // TODO Auto-generated method stub
    
  }

}
