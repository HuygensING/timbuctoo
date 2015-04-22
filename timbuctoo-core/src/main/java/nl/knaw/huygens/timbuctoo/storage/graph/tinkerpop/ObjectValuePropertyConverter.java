package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectValuePropertyConverter extends AbstractPropertyConverter {

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
  protected Object convert(Object value) {
    return value;
  }

}
