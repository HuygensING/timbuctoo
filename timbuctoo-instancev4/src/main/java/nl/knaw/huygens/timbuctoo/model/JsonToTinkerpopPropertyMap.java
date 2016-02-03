package nl.knaw.huygens.timbuctoo.model;

import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.timbuctoo.model.converters.Converter;
import nl.knaw.huygens.timbuctoo.model.converters.Converters;

import java.io.IOException;

public class JsonToTinkerpopPropertyMap {

  private final String jsonName;
  private final String tinkerpopName;
  private final Converter converter;

  public JsonToTinkerpopPropertyMap(String jsonName, String tinkerpopName) {
    this.jsonName = jsonName;
    this.tinkerpopName = tinkerpopName;
    this.converter = Converters.stringToString;
  }

  public JsonToTinkerpopPropertyMap(String jsonName, String tinkerpopName, Converter converter) {
    this.jsonName = jsonName;
    this.tinkerpopName = tinkerpopName;
    this.converter = converter;
  }

  public Object jsonToTinkerpop(JsonNode json) throws IOException {
    return this.converter.jsonToTinkerpop(json);
  }

  public String getJsonName() {
    return jsonName;
  }

  public String getTinkerpopName() {
    return tinkerpopName;
  }

  public JsonNode tinkerpopToJson(Object value) throws IOException {
    return this.converter.tinkerpopToJson(value);
  }
}
