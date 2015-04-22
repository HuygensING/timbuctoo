package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType;

import com.tinkerpop.blueprints.Vertex;

public abstract class AbstractPropertyConverter implements PropertyConverter {
  private Field field;
  private Class<? extends Entity> type;
  private FieldType fieldType;
  private String fieldName;

  @Override
  public void setField(Field field) {
    this.field = field;
  }

  @Override
  public void setContainingType(Class<? extends Entity> type) {
    this.type = type;
  }

  @Override
  public void setFieldType(FieldType fieldType) {
    this.fieldType = fieldType;
  }

  @Override
  public void setName(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public void setValueOfVertex(Vertex vertex, Entity entity) throws ConversionException {
    try {
      Object value = getValue(entity);

      if (isLegalValue(value)) {
        vertex.setProperty(propertyName(), format(value));
      }

    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new ConversionException(e);
    }
  }

  protected abstract Object format(Object value) throws IllegalArgumentException;

  protected boolean isLegalValue(Object value) {
    return value != null;
  }

  private String propertyName() {
    String propertyName = fieldType.propertyName(type, fieldName);
    return propertyName;
  }

  protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
    field.setAccessible(true);
    return field.get(entity);
  }

}
