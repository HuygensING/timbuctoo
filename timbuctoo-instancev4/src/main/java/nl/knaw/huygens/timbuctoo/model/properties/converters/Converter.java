package nl.knaw.huygens.timbuctoo.model.properties.converters;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface Converter {
  Object jsonToTinkerpop(JsonNode json) throws IOException;

  JsonNode tinkerpopToJson(Object value) throws IOException;

  default String getTypeIdentifier() {
    return null; //null indicates that the type is not available for inclusion in the metadata
  }
}
