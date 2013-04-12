package nl.knaw.huygens.repository.variation;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.storage.mongo.variation.DBJsonNode;

import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.mongodb.DBObject;

public class VariationReducer {
  private ObjectMapper mapper;
  private final DocumentTypeRegister docTypeRegistry;

  public VariationReducer(DocumentTypeRegister docTypeRegistry) {
    this.docTypeRegistry = docTypeRegistry;
    mapper = new ObjectMapper();
  }

  public VariationReducer(ObjectMapper mapper, DocumentTypeRegister docTypeRegistry) {
    this.mapper = mapper;
    this.docTypeRegistry = docTypeRegistry;
  }

  public <T extends Document> T reduceDBObject(DBObject obj, Class<T> cls) throws IOException {
    return reduceDBObject(obj, cls, null);
  }

  public <T extends Document> T reduceDBObject(DBObject obj, Class<T> cls, String variation) throws IOException {
    if (obj == null) {
      return null;
    }
    JsonNode tree = convertToTree(obj);
    return reduce(tree, cls, variation);
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

  public <T extends Document> T reduce(JsonNode node, Class<T> cls) throws VariationException, JsonProcessingException {
    return reduce(node, cls, null);
  }

  public <T extends Document> T reduce(JsonNode node, Class<T> cls, String requestedVariation) throws VariationException, JsonProcessingException {
    final String classVariation = VariationUtils.getVariationName(cls);
    String idPrefix = classVariation + "-";
    List<JsonNode> specificData = Lists.newArrayListWithExpectedSize(1);
    List<String> variations = getVariations(node);
    String requestedClassId = VariationUtils.getClassId(cls);

    String variationToRetrieve = null;
    JsonNode defaultVariationNode = null;

    if (node.get(requestedClassId) != null) {
      defaultVariationNode = node.get(requestedClassId).get(VariationUtils.DEFAULT_VARIATION);
    }

    variationToRetrieve = getVariationToRetrieve(classVariation, defaultVariationNode, requestedVariation, variations);

    ObjectNode rv = mapper.createObjectNode();
    for (Class<? extends Document> someCls : VariationUtils.getAllClasses(cls)) {
      String id = VariationUtils.getClassId(someCls);
      JsonNode data = node.get(id);
      if (data != null) {
        if (id.startsWith(idPrefix)) {
          specificData.add(data);
        } else {
          processCommonData(variationToRetrieve, data, rv);
        }
      }
    }
    for (JsonNode d : specificData) {
      if (d.isObject()) {
        rv.setAll((ObjectNode) d);
      } else {
        throw new VariationException("Non-object variation data; this should never happen.");
      }
    }
    Iterator<Entry<String, JsonNode>> nodeFields = node.fields();
    while (nodeFields.hasNext()) {
      Entry<String, JsonNode> entry = nodeFields.next();
      String key = entry.getKey();
      if (key.startsWith("^") || key.startsWith("_")) {
        rv.put(key, entry.getValue());
      }
    }
    T returnObject = mapper.treeToValue(rv, cls);
    returnObject.setVariations(variations);
    if (defaultVariationNode != null) {
      returnObject.setCurrentVariation(variationToRetrieve);
    }

    return returnObject;
  }

  private String getVariationToRetrieve(final String classVariation, JsonNode defaultVariationNode, String requestedVariation, List<String> variations) throws VariationException {
    String variationToGet = classVariation;
    if (VariationUtils.BASE_MODEL_PACKAGE_VARIATION.equals(classVariation)) {
      if (requestedVariation != null && isRequestedVariationAvailable(requestedVariation, variations)) {
        variationToGet = requestedVariation;
      } else {
        variationToGet = defaultVariationNode != null ? defaultVariationNode.asText() : classVariation;
      }
    } else if (requestedVariation != null && !classVariation.contains(requestedVariation)) {
      throw new VariationException("Variation does not exist for requested type.");
    }
    return variationToGet;
  }

  private boolean isRequestedVariationAvailable(String requestedVariation, List<String> projectVariations) {
    for (String variation : projectVariations) {
      if (variation.contains(requestedVariation)) {
        return true;
      }
    }
    return false;
  }

  private List<String> getVariations(JsonNode node) {
    List<String> variations = Lists.newArrayList();
    Iterator<Map.Entry<String, JsonNode>> fieldIterator = node.fields();

    Map.Entry<String, JsonNode> fieldEntry = null;

    while (fieldIterator.hasNext()) {
      fieldEntry = fieldIterator.next();
      if (fieldEntry.getValue() instanceof ObjectNode) {
        variations.add(fieldEntry.getKey());
      }
    }

    return variations;
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
      } else if (k.startsWith("!")) {} else {
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

  @SuppressWarnings("unchecked")
  private JsonNode convertToTree(DBObject obj) throws IOException {
    JsonNode tree;
    if (obj instanceof JacksonDBObject) {
      tree = ((JacksonDBObject<JsonNode>) obj).getObject();
    } else if (obj instanceof DBJsonNode) {
      tree = ((DBJsonNode) obj).getDelegate();
    } else {
      throw new IOException("Huh? DB didn't generate the right type of object out of the data stream...");
    }
    return tree;
  }

  public <T extends Document> List<T> getAllForDBObject(DBObject item, Class<T> cls) throws IOException {
    JsonNode jsonNode = convertToTree(item);
    Iterator<String> fieldNames = jsonNode.fieldNames();
    List<T> rv = Lists.newArrayList();
    while (fieldNames.hasNext()) {
      String f = fieldNames.next();
      if (!f.startsWith("^") && !f.startsWith("_")) {
        JsonNode subNode = jsonNode.get(f);
        if (subNode != null && subNode.isObject()) {
          @SuppressWarnings("unchecked")
          Class<? extends T> indicatedClass = (Class<? extends T>) docTypeRegistry.getClassFromTypeString(f);
          rv.add(reduce(jsonNode, indicatedClass));
        }
      }
    }
    return rv;
  }
}
