package nl.knaw.huygens.timbuctoo.model.converters;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public interface Converter {
  Object jsonToTinkerpop(JsonNode json) throws IOException;
}
