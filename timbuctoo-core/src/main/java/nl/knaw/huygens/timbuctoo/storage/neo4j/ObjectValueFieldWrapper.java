package nl.knaw.huygens.timbuctoo.storage.neo4j;

import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A FieldWrapper for the more complex values.
 */
public class ObjectValueFieldWrapper extends FieldWrapper {

  @Override
  public void addValueToNode(Node node) throws IllegalArgumentException, IllegalAccessException {
    try {
      Object fieldValue = getFieldValue();
      if (fieldValue != null) {
        node.setProperty(getName(), getSerializedValue(fieldValue));
      }
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private Object getSerializedValue(Object fieldValue) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(fieldValue);
  }
}
