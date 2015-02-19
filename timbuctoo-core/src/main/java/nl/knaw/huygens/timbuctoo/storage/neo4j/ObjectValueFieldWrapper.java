package nl.knaw.huygens.timbuctoo.storage.neo4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A FieldWrapper for the more complex values.
 */
public class ObjectValueFieldWrapper extends FieldWrapper {

  @Override
  protected Object getFormattedValue(Object fieldValue) throws IllegalArgumentException {
    try {
      return new ObjectMapper().writeValueAsString(fieldValue);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
