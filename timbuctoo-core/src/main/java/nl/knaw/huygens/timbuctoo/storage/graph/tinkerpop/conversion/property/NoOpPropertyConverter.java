package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.property;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion.PropertyConverter;

import com.tinkerpop.blueprints.Element;

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
  public String propertyName() {
    return fieldType.propertyName(type, fieldName);
  }

}
