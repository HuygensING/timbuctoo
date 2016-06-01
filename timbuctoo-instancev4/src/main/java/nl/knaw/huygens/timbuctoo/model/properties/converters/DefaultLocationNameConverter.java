package nl.knaw.huygens.timbuctoo.model.properties.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import nl.knaw.huygens.timbuctoo.experimental.exports.ExcelDescription;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.StringExcelDescription;
import nl.knaw.huygens.timbuctoo.model.LocationNames;

import java.io.IOException;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

public class DefaultLocationNameConverter implements Converter {

  @Override
  public Object jsonToTinkerpop(JsonNode json) throws IOException {
    throw new IOException("can only be converted to json, not from json.");
  }

  @Override
  public TextNode tinkerpopToJson(Object value) throws IOException {
    if (value instanceof String) {
      return jsn(new ObjectMapper().readValue((String) value, LocationNames.class).getDefaultName());
    } else {
      throw new IOException("should be a string");
    }
  }

  @Override
  public ExcelDescription tinkerPopToExcel(Object value, String guiTypeId) throws IOException {
    // FIXME: create an excel description for the full LocationNames class
    return new StringExcelDescription(tinkerpopToJson(value).asText(), guiTypeId);
  }
}
