package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.PropertyContainer;

public class NoOpFieldConverter implements FieldConverter {

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
    // TODO Auto-generated method stub

  }

  @Override
  public FieldType getFieldType() {
    return FieldType.VIRTUAL;
  }

  @Override
  public String getName() {
    return "";
  }

}
