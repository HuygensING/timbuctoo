package nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.property;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A FieldWrapper for the more complex values.
 */
class ObjectValuePropertyConverter extends AbstractPropertyConverter {

  private ObjectMapper objectMapper;

  public ObjectValuePropertyConverter() {
    objectMapper = new ObjectMapper();
  }

  @Override
  protected Object getFormattedValue(Object fieldValue) throws IllegalArgumentException {
    try {

      return objectMapper.writeValueAsString(fieldValue);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  protected Object convertValue(Object value, Class<?> fieldType) throws IllegalArgumentException {
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
