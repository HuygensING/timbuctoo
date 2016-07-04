package nl.knaw.huygens.timbuctoo.model.properties.converters;

import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.timbuctoo.experimental.exports.excel.description.ExcelDescription;

import java.io.IOException;

public interface Converter {
  Object jsonToTinkerpop(JsonNode json) throws IOException;

  JsonNode tinkerpopToJson(Object value) throws IOException;

  String getGuiTypeId();

  String getUniqueTypeIdentifier();

  ExcelDescription tinkerPopToExcel(Object value, String typeId) throws IOException;
}
