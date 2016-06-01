package nl.knaw.huygens.timbuctoo.model.properties.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import nl.knaw.huygens.timbuctoo.experimental.exports.ExcelDescription;

import java.io.IOException;

public class StringToStringConverter implements Converter {

  @Override
  public Object jsonToTinkerpop(JsonNode json) throws IOException {
    if (json.isTextual()) {
      return json.asText("");
    } else {
      throw new IOException("should be a string.");
    }
  }

  @Override
  public JsonNode tinkerpopToJson(Object value) throws IOException {
    if (value instanceof String) {
      return JsonNodeFactory.instance.textNode((String) value);
    } else {
      throw new IOException("should be a string");
    }
  }

  public String getTypeIdentifier() {
    return "text";
  }

  @Override
  public ExcelDescription tinkerPopToExcel(Object value) throws IOException {
    JsonNode json = tinkerpopToJson(value);
    // TODO: convert
    return new ExcelDescription();
  }
}
