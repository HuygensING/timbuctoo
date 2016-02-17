package nl.knaw.huygens.timbuctoo.model.properties.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.LambdaExceptionUtil.rethrowConsumer;

public class StringArrayToEncodedArrayOfLimitedValues implements Converter {
  private final List<String> allowedValues;
  private final ObjectMapper objectMapper;

  public StringArrayToEncodedArrayOfLimitedValues(String... allowedValues) {
    this.allowedValues = Lists.newArrayList(allowedValues);
    objectMapper = new ObjectMapper();
  }

  private void throwIfInvalid(JsonNode json) throws IOException {
    if (json instanceof ArrayNode) {
      json.forEach(rethrowConsumer(val -> {
        StringToEncodedStringOfLimitedValuesConverter.throwIfInvalid(val, this.allowedValues);
      }));
    } else {
      throw new IOException("should be an array.");
    }
  }

  @Override
  public Object jsonToTinkerpop(JsonNode json) throws IOException {
    throwIfInvalid(json);
    return json.toString();
  }

  @Override
  public ArrayNode tinkerpopToJson(Object value) throws IOException {
    if (value instanceof String) {
      JsonNode result = objectMapper.readTree((String) value);
      throwIfInvalid(result);
      return (ArrayNode) result;
    } else {
      throw new IOException("should be a string");
    }
  }
}
