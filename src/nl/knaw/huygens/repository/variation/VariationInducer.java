package nl.knaw.huygens.repository.variation;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import nl.knaw.huygens.repository.model.Document;

public class VariationInducer {
  private ObjectMapper mapper;
  private Class<?> view;
  private ObjectWriter writerWithView;

  public VariationInducer() {
    mapper = new ObjectMapper();
    view = null;
    writerWithView = mapper.writerWithView(view);
  }
  
  public VariationInducer(ObjectMapper mapper) {
    this.mapper = mapper;
    this.view = null;
    writerWithView = mapper.writerWithView(view);
  }
  
  public VariationInducer(ObjectMapper mapper, Class<?> view) {
    this.mapper = mapper;
    this.view = view;
    writerWithView = mapper.writerWithView(view);
  }
  
  public <T extends Document> JsonNode induce(T item, Class<T> cls) throws VariationException {
    ObjectNode treeNode = mapper.createObjectNode();
    treeNode.put(VariationUtils.COMMON_PROPS, mapper.createObjectNode());
    return induce(item, cls, treeNode);
  }
  
  public <T extends Document> JsonNode induce(T item, Class<T> cls, ObjectNode existingItem) throws VariationException {
    Class<?> commonClass = getEarliestCommonClass(cls);
    JsonNode commonTree = asTree(item, commonClass);
    JsonNode completeTree = asTree(item, cls);

    JsonNode existingCommonNode = existingItem.get(VariationUtils.COMMON_PROPS);
    if (existingCommonNode == null || !existingCommonNode.isObject()) {
      throw new VariationException("Common object is not an object?");
    }
    ObjectNode existingCommonTree = (ObjectNode) existingCommonNode;
    
    String variation = VariationUtils.getVariationName(cls);
    if (!existingItem.has(variation)) {
      existingItem.put(variation, mapper.createObjectNode());
    }
    JsonNode existingVariation = existingItem.get(variation);
    if (!existingVariation.isObject()) {
      throw new VariationException("Variation object (" + variation + ") is not an object?");
    }
    ObjectNode variationNode = (ObjectNode) existingVariation;
    
    Iterator<Entry<String, JsonNode>> fields = completeTree.fields();
    while (fields.hasNext()) {
      Entry<String, JsonNode> field = fields.next();
      String k = field.getKey();
      JsonNode fieldNode = field.getValue();
      /* For each property, there are 3 possibilities:
       * a) it is a prefixed (^ or _) property, which should always be the same among all variations
       *    and is used for identifying different objects, their version, etc.
       * b) it is common between different variations (project/VRE/whatever)
       * c) it is specific to a single variation (project/VRE/whatever)
       */
      if (k.startsWith("^") || k.startsWith("_")) {
        // Either this is a new object and we need to add the property, or it is an existing one in which
        // case we should check for an exact match:
        if (!existingItem.has(k)) {
          existingItem.put(k, fieldNode);
        } else if (!fieldNode.equals(existingItem.get(k))) {
          throw new VariationException("Inducing object into wrong object; fields " + k + " are not equal (" +
                                       fieldNode.toString() + " vs. " + existingItem.get(k).toString() + "!");
        }
      } else if (commonTree.has(k)) {
        addOrMergeVariation(existingCommonTree, k, variation, fieldNode);
      } else {
        variationNode.put(k, fieldNode);
      }
    }
    return existingItem;
  }
  
  private void addOrMergeVariation(ObjectNode existingCommonTree, String key, String variationId, JsonNode variationValue) throws VariationException {
    // Find the right property variation array, create it if it does not exist yet:
    if (!existingCommonTree.has(key)) {
      addVariation(existingCommonTree, key);
    }
    ArrayNode existingValueAry = cautiousGetArray(existingCommonTree, key);
    
    // Look through the array and remove us from things we no longer agree with, add to the thing we do agree with:
    int i = 0;
    boolean foundValue = false;
    boolean foundKey = false;
    Iterator<JsonNode> elements = existingValueAry.elements();
    while (elements.hasNext()) {
      JsonNode value = elements.next();
      if (!value.isObject()) {
        throw new VariationException("Variation for '" + key + "', index " + i + " is not an object?!");
      }

      JsonNode actualValue = value.get(VariationUtils.VALUE);
      ArrayNode agreedValueAry = cautiousGetArray(value, VariationUtils.AGREED);
      boolean thisValueIsCorrect = actualValue.equals(variationValue);
      foundValue = foundValue || thisValueIsCorrect;

      int agreedIndex = arrayIndexOf(agreedValueAry, variationId);
      // Are we currently listed as agreeing with this?
      if (agreedIndex != -1) {
        // ... while we shouldn't?
        if (!thisValueIsCorrect) {
          agreedValueAry.remove(agreedIndex);
          
          // If nobody agrees with this value anymore; purge it:
          if (agreedValueAry.size() == 0) {
            elements.remove();
          }
        }
        foundKey = true;
      } else if (thisValueIsCorrect) {
        // we're not listed but we should be:
        agreedValueAry.add(variationId);
      }
      if (foundValue && foundKey) {
        break;
      }
      i++;
    }
    // We didn't find the right value anywhere, add ourselves:
    if (!foundValue) {
      addVariationItem(existingValueAry, variationId, variationValue);
    }
  }

  private int arrayIndexOf(ArrayNode agreedValueAry, String variationId) {
    int i = agreedValueAry.size();
    while (i-- > 0) {
      JsonNode agreedValue = agreedValueAry.get(i);
      // Found us as saying we agree with this value:
      if (variationId.equals(agreedValue.asText())) {
        return i;
      }
    }
    return -1;
  }

  private ArrayNode cautiousGetArray(JsonNode obj, String key) throws VariationException {
    JsonNode val = obj.get(key);
    if (val == null || !val.isArray()) {
      throw new VariationException("Value for '" + key + "' is not an array?!");
    }
    return (ArrayNode) val;
  }

  private void addVariationItem(ArrayNode existingValueAry, String variationId, JsonNode variationValue) {
    ObjectNode var = mapper.createObjectNode();
    ArrayNode agreedList = mapper.createArrayNode();
    agreedList.add(variationId);
    var.put(VariationUtils.AGREED, agreedList);
    var.put(VariationUtils.VALUE, variationValue);
    existingValueAry.add(var);
  }

  private void addVariation(ObjectNode existingCommonTree, String key) {
    ArrayNode n = mapper.createArrayNode();
    existingCommonTree.put(key, n);
  }

  private Class<?> getEarliestCommonClass(Class<?> cls) {
    while (cls != null) {
      if (VariationUtils.getVariationName(cls).equals(VariationUtils.BASE_MODEL_PACKAGE_VARIATION)) {
        return cls;
      }
      cls = cls.getSuperclass();
    }
    return null;
  }
  
  /**
   * This is a modified copy of the built-in "valueAsTree" method on ObjectMapper.
   * The modification includes being able to specify the view and the type used
   * for serialization
   * @param val Value to serialize
   * @param cls Type to use for serializing the value (should be on the type chain of the value's runtime type)
   * @return a JSON tree representation of the object
   * @throws IllegalArgumentException
   */
  private JsonNode asTree(Object val, Class<?> cls) throws IllegalArgumentException {
    if (val == null) {
      return null;
    }
    TokenBuffer buf = new TokenBuffer(mapper);
    JsonNode result;
    try {
      writerWithView.withType(cls).writeValue(buf, val);
      JsonParser jp = buf.asParser();
      result = mapper.readTree(jp);
      jp.close();
    } catch (IOException e) { // should not occur, no real i/o...
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return result;
  }

}
