package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.Node;

public class NoOpFieldWrapper implements FieldConverter {

  @Override
  public void setContainingType(Class<? extends Entity> containingType) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setField(Field field) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setNodeProperty(Node node, Entity entity) throws ConversionException {
    // TODO Auto-generated method stub

  }

  @Override
  public void addValueToEntity(Entity entity, Node node) throws ConversionException {
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

}
