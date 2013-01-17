package nl.knaw.huygens.repository.variation;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.mongodb.DBObject;

import net.vz.mongodb.jackson.internal.stream.JacksonDBObject;

import nl.knaw.huygens.repository.model.Document;

public class VariationReducer {
  private ObjectMapper mapper;

  public VariationReducer() {
    mapper = new ObjectMapper();
  }
  
  public VariationReducer(ObjectMapper mapper) {
    this.mapper = mapper;
  }
  
  
  public <T extends Document> List<T> reduceDBObject(List<DBObject> nodes, Class<T> cls) throws IOException {
    List<T> rv = Lists.newArrayListWithCapacity(nodes.size());
    for (DBObject n : nodes) {
      rv.add(reduceDBObject(n, cls));
    }
    return rv;
  }
  
  public <T extends Document> List<T> reduce(List<JsonNode> nodes, Class<T> cls) throws VariationException, JsonProcessingException {
    List<T> rv = Lists.newArrayListWithCapacity(nodes.size());
    for (JsonNode n : nodes) {
      rv.add(reduce(n, cls));
    }
    return rv;
  }
  
  public <T extends Document> T reduce(JsonNode n, Class<T> cls) throws VariationException, JsonProcessingException {
    final String variationName = VariationUtils.getVariationName(cls);
    JsonNode commonData = n.get(VariationUtils.COMMON_PROPS);
    JsonNode specificData = n.get(variationName);
    ObjectNode rv = mapper.createObjectNode();
    if (commonData != null) {
      processCommonData(variationName, commonData, rv);
    }
    if (specificData != null) {
      if (specificData.isObject()) {
        rv.setAll((ObjectNode) specificData);
      } else {
        throw new VariationException("Non-object variation data; this should never happen.");
      }
    }
    Iterator<Entry<String, JsonNode>> nodeFields = n.fields();
    while (nodeFields.hasNext()) {
      Entry<String, JsonNode> entry = nodeFields.next();
      String key = entry.getKey();
      if (key.startsWith("^") || key.startsWith("_")) {
        rv.put(key, entry.getValue());
      }
    }
    return mapper.treeToValue(rv, cls);
  }

  private void processCommonData(final String variationName, JsonNode commonData, ObjectNode rv) throws VariationException {
    Iterator<Entry<String, JsonNode>> fields = commonData.fields(); 
    // Go through all common fields:
    while (fields.hasNext()) {
      Entry<String, JsonNode> f = fields.next();
      String k = f.getKey();
      JsonNode fV = f.getValue();
      // Loop through values:
      if (fV.isArray()) {
        ArrayNode ary = (ArrayNode) fV;
        fetchAndAssignMatchingValue(variationName, rv, k, ary);
      } else {
        throw new VariationException("Unknown variation value for key " + k);
      }
    }
  }

  private void fetchAndAssignMatchingValue(final String variationName, ObjectNode rv, String k, ArrayNode ary) throws VariationException {
    int i = 0;
    for (JsonNode elem : ary) {
      if (elem.isObject()) {
        // Check the list of agreeing VREs to see if we want this one:
        JsonNode agreedValueNode = elem.get(VariationUtils.AGREED);
        if (agreedValueNode != null && agreedValueNode.isArray()) {
          ArrayNode agreedValues = (ArrayNode) agreedValueNode;
          if (arrayContains(agreedValues, variationName)) {
            rv.put(k, elem.get(VariationUtils.VALUE));
            return;
          }
        } else {
          throw new VariationException("Unknown variation 'agreed' object for key " + k + " and index " + i);
        }
      } else {
        throw new VariationException("Unknown variation array element for key " + k + " and index " + i);
      }
      i++;
    }
    // The loop will return as we found a value that agrees;
    // if we get here that means no such value exists, so
    // we will put null:
    rv.putNull(k);
  }

  private boolean arrayContains(ArrayNode stringAry, String stringEl) {
    // I assume there is a better way to do this but I have not found it:
    int i = stringAry.size();
    while (i-- > 0) {
      if (stringAry.get(i).asText().equals(stringEl)) {
        return true;
      }
    }
    return false;
  }

  public void setMapper(ObjectMapper mapper) {
    this.mapper = mapper;
  }
  

  public <T extends Document> T reduceDBObject(DBObject obj, Class<T> cls) throws VariationException, JsonProcessingException, IOException {
    if (obj == null) {
      return null;
    }
    if (obj instanceof JacksonDBObject) {
      @SuppressWarnings("unchecked")
      JsonNode tree = ((JacksonDBObject<JsonNode>) obj).getObject();
      return reduce(tree, cls);
    }

    throw new IOException("Huh? DB didn't generate the right type of object out of the data stream...");
  }
}
