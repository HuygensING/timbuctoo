package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType;

public class ObjectValuePropertyConverter implements PropertyConverter {

  @Override
  public void setField(Field field) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void setContainingType(Class<? extends Entity> type) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void setFieldType(FieldType fieldType) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

  @Override
  public void setName(String fieldName) {
    throw new UnsupportedOperationException("Yet to be implemented");
  }

}
