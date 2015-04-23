package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class ObjectValuePropertyConverter extends AbstractPropertyConverter {

  private final ObjectMapper objectMapper;

  public ObjectValuePropertyConverter() {
    objectMapper = new ObjectMapper();
  }

  @Override
  protected Object format(Object value) throws IllegalArgumentException {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  protected Object convert(Object value, Class<?> fieldType) {
    if (!(value instanceof String)) {
      throw new IllegalArgumentException("Value should be a String");
    }
    try {
      return objectMapper.readValue(value.toString(), fieldType);
    } catch (IOException e) {
      throw new IllegalArgumentException("Value could not be read.");
    }
  }

}
