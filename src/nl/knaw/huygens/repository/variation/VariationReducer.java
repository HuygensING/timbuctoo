package nl.knaw.huygens.repository.variation;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import nl.knaw.huygens.repository.model.Document;

public class VariationReducer {
  public static class VariationException extends IOException {
    private static final long serialVersionUID = 2225153974182989864L;
    public VariationException(String msg) {
      super(msg);
    }
  }

  public static <T extends Document> T reduce(JsonNode n, Class<T> cls) throws VariationException, JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    final String variationName = getVariationName(cls);
    JsonNode commonData = n.get("common");
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
    return mapper.treeToValue(rv, cls);
  }

  private static void processCommonData(final String variationName, JsonNode commonData, ObjectNode rv) throws VariationException {
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

  private static void fetchAndAssignMatchingValue(final String variationName, ObjectNode rv, String k, ArrayNode ary) throws VariationException {
    int i = 0;
    for (JsonNode elem : ary) {
      if (elem.isObject()) {
        // Check the list of agreeing VREs to see if we want this one:
        JsonNode agreedValueNode = elem.get("agreed");
        if (agreedValueNode.isArray()) {
          ArrayNode agreedValues = (ArrayNode) agreedValueNode;
          if (arrayContains(agreedValues, variationName)) {
            rv.put(k, elem.get("v"));
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

  private static boolean arrayContains(ArrayNode stringAry, String stringEl) {
    // I assume there is a better way to do this but I have not found it:
    int i = stringAry.size();
    while (i-- > 0) {
      if (stringAry.get(i).asText().equals(stringEl)) {
        return true;
      }
    }
    return false;
  }

  private static String getVariationName(Class<?> cls) {
    String packageName = cls.getPackage().getName();
    final String variationName = packageName.substring(packageName.lastIndexOf('.') + 1);
    return variationName;
  }
}
