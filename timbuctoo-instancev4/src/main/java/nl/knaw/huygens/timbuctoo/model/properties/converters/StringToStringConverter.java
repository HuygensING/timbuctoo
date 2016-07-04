package nl.knaw.huygens.timbuctoo.model.properties.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.StringExcelDescription;

import java.io.IOException;

public class StringToStringConverter implements Converter {

  static final String TYPE = "string";

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

  public String getGuiTypeId() {
    return "text";
  }

  @Override
  public String getUniqueTypeIdentifier() {
    return TYPE;
  }

  @Override
  public ExcelDescription tinkerPopToExcel(Object value, String typeId) throws IOException {
    return new StringExcelDescription(tinkerpopToJson(value).asText(), typeId);
  }
}
