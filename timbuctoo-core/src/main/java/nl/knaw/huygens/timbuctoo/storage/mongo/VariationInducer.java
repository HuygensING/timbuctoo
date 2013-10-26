package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.mongojack.internal.stream.JacksonDBObject;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.DBObject;

class VariationInducer extends VariationConverter {

  private final ObjectWriter writer;

  public VariationInducer(TypeRegistry registry, Class<?> view) {
    super(registry);
    writer = mapper.writerWithView(view);
  }

  public <T extends Entity> JsonNode induce(T item, Class<T> type) throws VariationException {
    return induce(item, type, (ObjectNode) null);
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induce(T item, Class<T> type, DBObject existingItem) throws VariationException {
    ObjectNode node;
    if (existingItem == null) {
      node = createNode(null, type, getVariationNamesForType(type));
    } else if (existingItem instanceof JacksonDBObject) {
      node = (ObjectNode) (((JacksonDBObject<JsonNode>) existingItem).getObject());
    } else if (existingItem instanceof DBJsonNode) {
      node = (ObjectNode) ((DBJsonNode) existingItem).getDelegate();
    } else {
      throw new VariationException("Unknown type of DBObject!");
    }
    return induce(item, type, node);
  }

  private <T extends Entity> ObjectNode createNode(ObjectNode node, Class<T> type, List<String> variationNames) {
    if (node == null) {
      node = mapper.createObjectNode();
    }
    for (String name : variationNames) {
      if (!node.has(name)) {
        node.put(name, mapper.createObjectNode());
      }
    }
    return node;
  }

  public <T extends Entity> JsonNode induce(T item, Class<T> type, ObjectNode existingItem) throws VariationException {
    Preconditions.checkArgument(item != null);
    Preconditions.checkArgument(type != null);

    String variationId = getPackageName(type);
    List<String> variationNames = getVariationNamesForType(type);
    existingItem = createNode(existingItem, type, variationNames);
    Map<String, Object> finishedKeys = Maps.newHashMap();

    for (String variationName : Lists.reverse(variationNames)) {
      JsonNode obj = existingItem.get(variationName);
      if (!obj.isObject()) {
        throw new VariationException("Node (" + variationName + ") is not an object");
      }
      ObjectNode currentClsNode = (ObjectNode) obj;

      JsonNode itemTree = asTree(item, variationName);
      boolean isShared = !variationName.startsWith(variationId + "-");

      for (Entry<String, JsonNode> field : ImmutableList.copyOf(itemTree.fields())) {
        String key = field.getKey();
        JsonNode fieldNode = field.getValue();

        // Should not store bits we already stored in other parts of the hierarchy:
        if (finishedKeys.containsKey(key) && fieldNode.equals(finishedKeys.get(key))) {
          continue;
        }
        if (!key.startsWith("!")) {
          finishedKeys.put(key, fieldNode);
        }

        /*
         * For each property, there are 5 possibilities:
         * a) it is prefixed with an @, this means that it is only used in the
         * application and should be removed when the object is saved in the database.
         * b) it is a prefixed (^ or _) property, which should always be the same among
         * all variations and is used for identifying objects, their version, etc.
         * c) it is shared between different variations (project/VRE/whatever)
         * d) it is specific to a single variation (project/VRE/whatever)
         * e) it is prefixed with a !, this means this property is present
         * in multiple object and represents some default value.
         */
        if (key.startsWith("@")) {
          // ignore field.
        } else if (key.startsWith("^") || key.startsWith("_")) {
          // Either this is a new object and we need to add the property, or it
          // is an existing one in which case we should check for an exact match:
          if (!existingItem.has(key)) {
            existingItem.put(key, fieldNode);
          } else if (!fieldNode.equals(existingItem.get(key))) {
            throw new VariationException("Inducing object into wrong object; fields " + key + " are not equal (" + fieldNode.toString() + " vs. " + existingItem.get(key).toString() + "!");
          }
        } else if (key.equals("!currentVariation") && isShared) { //only for shared classes a defaultVRE should be added.
          if (existingItem.get(variationName) != null && existingItem.get(variationName).get(DEFAULT_VARIATION) == null) {
            currentClsNode.put(DEFAULT_VARIATION, variationId);
          }
        } else if (isShared) {
          addOrMergeVariation(currentClsNode, key, variationId, fieldNode);
        } else if (!key.equals("!currentVariation")) {
          currentClsNode.put(key, fieldNode);
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

      JsonNode actualValue = value.get(VALUE);
      ArrayNode agreedValueAry = cautiousGetArray(value, AGREED);
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
    var.put(AGREED, agreedList);
    var.put(VALUE, variationValue);
    existingValueAry.add(var);
  }

  /**
   * This is a modified copy of the built-in "valueAsTree" method on ObjectMapper.
   * It allows to specify the view and the type used for serialization
   * 
   * @param value
   *          Value to serialize
   * @param variationName
   *          Variation name to use for serializing
   * @return a JSON tree representation of the object
   */
  private JsonNode asTree(Object value, String variationName) {
    try {
      Class<?> rootType = variationNameToType(variationName);
      TokenBuffer buffer = new TokenBuffer(mapper);
      writer.withType(rootType).writeValue(buffer, value);
      JsonParser parser = buffer.asParser();
      JsonNode result = mapper.readTree(parser);
      parser.close();
      return result;
    } catch (IOException e) { // should not occur, no real i/o...
      throw new IllegalStateException("Impossible");
    }
  }

}
