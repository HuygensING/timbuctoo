package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.PropertyConverter;

import com.tinkerpop.blueprints.Element;

abstract class AbstractPropertyConverter implements PropertyConverter {
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
  public FieldType getFieldType() {
    return fieldType;
  }

  @Override
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  @Override
  public final void setPropertyOfElement(Element element, Entity entity) throws ConversionException {
    try {
      Object value = getValue(entity);

      String propertyName = propertyName();
      if (isLegalValue(value)) {
        element.setProperty(propertyName, format(value));
      } else {
        element.removeProperty(propertyName);
      }

    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new ConversionException(e);
    }
  }

  protected abstract Object format(Object value) throws IllegalArgumentException;

  protected boolean isLegalValue(Object value) {
    return value != null;
  }

  @Override
  public String propertyName() {
    return fieldType.propertyName(type, fieldName);
  }

  protected Object getValue(Entity entity) throws IllegalAccessException, IllegalArgumentException {
    field.setAccessible(true);
    return field.get(entity);
  }

  @Override
  public final void addValueToEntity(Entity entity, Element element) throws ConversionException {
    Object value = element.getProperty(propertyName());

    try {
      fillField(entity, value);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new ConversionException(e);
    }
  }

  protected abstract Object convert(Object value, Class<?> fieldType);

  protected void fillField(Entity entity, Object value) throws IllegalAccessException, IllegalArgumentException {
    if (value != null) {
      field.setAccessible(true);
      field.set(entity, convert(value, field.getType()));
    }
  }

  @Override
  public void removeFrom(Element element) {
    element.removeProperty(propertyName());
  }
}
