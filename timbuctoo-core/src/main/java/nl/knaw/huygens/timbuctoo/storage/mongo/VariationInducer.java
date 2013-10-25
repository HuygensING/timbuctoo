package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mongodb.DBObject;

public class VariationInducer {

  private final ObjectMapper mapper;
  private final ObjectWriter writerWithView;

  public VariationInducer(Class<?> view) {
    mapper = new ObjectMapper();
    writerWithView = mapper.writerWithView(view);
  }

  public VariationInducer() {
    this(null);
  }

  public <T extends Entity> JsonNode induce(T item, Class<T> type) throws VariationException {
    return induce(item, type, (ObjectNode) null);
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induce(T item, Class<T> type, DBObject existingItem) throws VariationException {
    ObjectNode o;
    if (existingItem == null) {
      List<Class<? extends Entity>> classIds = VariationUtils.getAllClasses(type);
      o = createNode(null, type, classIds);
    } else if (existingItem instanceof JacksonDBObject) {
      o = (ObjectNode) (((JacksonDBObject<JsonNode>) existingItem).getObject());
    } else if (existingItem instanceof DBJsonNode) {
      o = (ObjectNode) ((DBJsonNode) existingItem).getDelegate();
    } else {
      throw new VariationException("Unknown type of DBObject!");
    }
    return induce(item, type, o);
  }

  private <T extends Entity> ObjectNode createNode(ObjectNode node, Class<T> type, List<Class<? extends Entity>> allClasses) {
    if (node == null) {
      node = mapper.createObjectNode();
    }
    for (Class<? extends Entity> someCls : allClasses) {
      String classId = VariationUtils.typeToVariationName(someCls);
      if (!node.has(classId)) {
        node.put(classId, mapper.createObjectNode());
      }
    }
    return node;
  }

  public <T extends Entity> JsonNode induce(T item, Class<T> type, ObjectNode existingItem) throws VariationException {
    Preconditions.checkArgument(item != null);
    Preconditions.checkArgument(type != null);

    String variationId = VariationUtils.getPackageName(type);
    List<Class<? extends Entity>> allClasses = VariationUtils.getAllClasses(type);
    existingItem = createNode(existingItem, type, allClasses);
    int size = allClasses.size();
    int i = size;
    Map<String, Object> finishedKeys = Maps.newHashMap();
    while (i-- > 0) {
      Class<? extends Entity> someCls = allClasses.get(i);
      String classId = VariationUtils.typeToVariationName(someCls);
      JsonNode obj = existingItem.get(classId);
      if (!obj.isObject()) {
        throw new VariationException("Variation object (" + classId + ") is not an object?");
      }

      ObjectNode currentClsNode = (ObjectNode) obj;
      JsonNode itemTree = asTree(item, someCls);
      boolean isShared = !classId.startsWith(variationId + "-");

      Iterator<Entry<String, JsonNode>> fields = itemTree.fields();
      while (fields.hasNext()) {
        Entry<String, JsonNode> field = fields.next();
        String k = field.getKey();
        JsonNode fieldNode = field.getValue();

        // Should not store bits we already stored in other parts of the hierarchy:
        if (finishedKeys.containsKey(k) && fieldNode.equals(finishedKeys.get(k))) {
          continue;
        }
        if (!k.startsWith("!")) {
          finishedKeys.put(k, fieldNode);
        }

        /*
         * For each property, there are 5 possibilities: a) it is prefixed with
         * an @, this means that it is only used in the application and should
         * be removed when the object is saved in the database. b) it is a
         * prefixed (^ or _) property, which should always be the same among all
         * variations and is used for identifying different objects, their
         * version, etc. c) it is shared between different variations
         * (project/VRE/whatever) d) it is specific to a single variation
         * (project/VRE/whatever) e) it is prefixed with a !, this means this
         * property is present in multiple object and represents some default
         * value.
         */
        if (k.startsWith("@")) {
          // ignore field.
        } else if (k.startsWith("^") || k.startsWith("_")) {
          // Either this is a new object and we need to add the property, or it
          // is an existing one in which case we should check for an exact match:
          if (!existingItem.has(k)) {
            existingItem.put(k, fieldNode);
          } else if (!fieldNode.equals(existingItem.get(k))) {
            throw new VariationException("Inducing object into wrong object; fields " + k + " are not equal (" + fieldNode.toString() + " vs. " + existingItem.get(k).toString() + "!");
          }
        } else if (k.equals("!currentVariation") && isShared) { //only for shared classes a defaultVRE should be added.
          if (existingItem.get(classId) != null && existingItem.get(classId).get(VariationUtils.DEFAULT_VARIATION) == null) {
            currentClsNode.put(VariationUtils.DEFAULT_VARIATION, variationId);
          }
        } else if (isShared) {
          addOrMergeVariation(currentClsNode, k, variationId, fieldNode);
        } else if (!k.equals("!currentVariation")) {
          currentClsNode.put(k, fieldNode);
        }
      }
    }
    return existingItem;
  }

  private void addOrMergeVariation(ObjectNode existingCommonTree, String key, String variationId, JsonNode variationValue) throws VariationException {
    // Find the right property variation array, create it if it does not exist yet:
    if (!existingCommonTree.has(key)) {
      existingCommonTree.put(key, mapper.createArrayNode());
    }
    ArrayNode existingValueAry = cautiousGetArray(existingCommonTree, key);

    // Look through the array and remove us from things we no longer agree with,
    // add to the thing we do agree with:
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

  /**
   * This is a modified copy of the built-in "valueAsTree" method on
   * ObjectMapper. The modification includes being able to specify the view and
   * the type used for serialization
   * 
   * @param val
   *          Value to serialize
   * @param cls
   *          Type to use for serializing the value (should be on the type chain
   *          of the value's runtime type)
   * @return a JSON tree representation of the object
   * @throws IllegalArgumentException
   */
  private JsonNode asTree(Object val, Class<?> cls) throws IllegalArgumentException {
    if (val == null) {
      return null;
    }
    TokenBuffer buffer = new TokenBuffer(mapper);
    JsonNode result;
    try {
      writerWithView.withType(cls).writeValue(buffer, val);
      JsonParser parser = buffer.asParser();
      result = mapper.readTree(parser);
      parser.close();
    } catch (IOException e) { // should not occur, no real i/o...
      throw new IllegalArgumentException(e.getMessage(), e);
    }
    return result;
  }

}
