package nl.knaw.huygens.timbuctoo.model.properties.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import nl.knaw.huygens.timbuctoo.experimental.exports.ExcelDescription;

import java.io.IOException;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

public class DefaultFullPersonNameConverter implements Converter {
  private final PersonNamesConverter converter;

  public DefaultFullPersonNameConverter() {
    converter = new PersonNamesConverter();
  }

  @Override
  public Object jsonToTinkerpop(JsonNode json) throws IOException {
    throw new IOException("can only be converted to json, not from json.");
  }

  @Override
  public TextNode tinkerpopToJson(Object value) throws IOException {
    return jsn(converter.tinkerpopToJava(value).defaultName().getFullName());
  }

  @Override
  public ExcelDescription tinkerPopToExcel(Object value) throws IOException {
    JsonNode json = tinkerpopToJson(value);
    // TODO: convert
    return new ExcelDescription();
  }
}
