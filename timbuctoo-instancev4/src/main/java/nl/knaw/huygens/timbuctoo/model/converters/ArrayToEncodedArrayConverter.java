package nl.knaw.huygens.timbuctoo.model.converters;

import com.fasterxml.jackson.databind.JsonNode;

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
}
