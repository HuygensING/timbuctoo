package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

import com.tinkerpop.blueprints.Element;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.PropertyConverter;

import java.lang.reflect.Field;

class NoOpPropertyConverter implements PropertyConverter {

  private String fieldName;
  private FieldType fieldType;
  private Class<? extends Entity> type;

  @Override
  public void setField(Field field) {}

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
    return this.fieldType;
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
  public void setPropertyOfElement(Element element, Entity entity) throws ConversionException {}

  @Override
  public void addValueToEntity(Entity entity, Element element) throws ConversionException {}

  @Override
  public String completePropertyName() {
    return fieldType.completePropertyName(type, fieldName);
  }

  @Override
  public void setPropertyName(String propertyName) {

  }

  @Override
  public void removeFrom(Element element) {}

}
