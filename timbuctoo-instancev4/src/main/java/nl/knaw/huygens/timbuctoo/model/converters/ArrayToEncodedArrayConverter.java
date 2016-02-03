package nl.knaw.huygens.timbuctoo.model.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;

class ArrayToEncodedArrayConverter implements Converter {
  @Override
  public Object jsonToTinkerpop(JsonNode json) throws IOException {
    if (json.isArray()) {
      return json.toString();
    } else {
      throw new IOException("should be an Array.");
    }
  }

  ObjectMapper mapper = new ObjectMapper();

  @Override
  public JsonNode tinkerpopToJson(Object value) throws IOException {
    if (value instanceof String) {
      JsonNode jsonNode = mapper.readTree((String) value);
      if (jsonNode instanceof ArrayNode) {
        return (ArrayNode) jsonNode;
      } else {
        throw new IOException("is encoded JSON, but not an array: " + jsonNode.toString());
      }
    } else {
      throw new IOException("should be an string encoded Array");
    }
  }
}
