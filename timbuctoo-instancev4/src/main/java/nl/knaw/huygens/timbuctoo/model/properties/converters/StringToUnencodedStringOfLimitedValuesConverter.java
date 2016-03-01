package nl.knaw.huygens.timbuctoo.model.properties.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;

public class StringToUnencodedStringOfLimitedValuesConverter implements Converter, HasOptions {
  private final ArrayList<String> allowedValues;

  public StringToUnencodedStringOfLimitedValuesConverter(String[] values) {
    this.allowedValues = Lists.newArrayList(values);
  }

  @Override
  public Object jsonToTinkerpop(JsonNode json) throws IOException {
    StringToEncodedStringOfLimitedValuesConverter.throwIfInvalid(json, this.allowedValues);
    return json.asText();
  }

  @Override
  public TextNode tinkerpopToJson(Object value) throws IOException {
    if (value instanceof String) {
      JsonNode result = jsn((String) value);
      StringToEncodedStringOfLimitedValuesConverter.throwIfInvalid(result, this.allowedValues);
      return (TextNode) result;
    } else {
      throw new IOException("should be a string");
    }
  }

  public String getTypeIdentifier() {
    return "select";
  }

  @Override
  public Collection<String> getOptions() {
    return this.allowedValues;
  }
}
