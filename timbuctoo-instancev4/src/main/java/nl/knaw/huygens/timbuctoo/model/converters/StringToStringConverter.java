package nl.knaw.huygens.timbuctoo.model.converters;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

class StringToStringConverter implements Converter {

  @Override
  public Object jsonToTinkerpop(JsonNode json) throws IOException {
    if (json.isTextual()) {
      return json.asText();
    } else {
      throw new IOException("should be a string.");
    }
  }
}
