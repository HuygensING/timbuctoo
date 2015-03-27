package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.property;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.neo4j.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.FieldType;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyConverter;

import org.neo4j.graphdb.PropertyContainer;

class NoOpPropertyConverter implements PropertyConverter {

  private String fieldName;

  @Override
  public void setContainingType(Class<? extends Entity> containingType) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setField(Field field) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setPropertyContainerProperty(PropertyContainer propertyContainer, Entity entity) throws ConversionException {
    // TODO Auto-generated method stub

  }

  @Override
  public void addValueToEntity(Entity entity, PropertyContainer propertyContainer) throws ConversionException {
    // TODO Auto-generated method stub

  }

  @Override
  public void setFieldType(FieldType fieldType) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setName(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public FieldType getFieldType() {
    return FieldType.VIRTUAL;
  }

  @Override
  public String getName() {
    return this.fieldName;
  }

  @Override
  public String getPropertyName() {
    return fieldName;
  }

}
