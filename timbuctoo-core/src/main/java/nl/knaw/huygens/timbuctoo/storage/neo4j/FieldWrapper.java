package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.neo4j.graphdb.Node;

public abstract class FieldWrapper {

  private Field field;
  private SystemEntity entity;
  private NameCreator nameCreator;

  public void setContainingEntity(SystemEntity entity) {
    this.entity = entity;
  }

  public void setField(Field field) {
    this.field = field;
  }

  public void setPropertyNameCreator(NameCreator nameCreator) {
    this.nameCreator = nameCreator;
  }

  protected Object getFieldValue() throws IllegalArgumentException, IllegalAccessException {
    field.setAccessible(true);
    return field.get(entity);
  }

  public abstract void addValueToNode(Node node) throws IllegalArgumentException, IllegalAccessException;

  protected String getName() {
    return nameCreator.property(getContainingType(), getFieldName());
  }

  private String getFieldName() {
    return field.getName();
  }

  private Class<? extends SystemEntity> getContainingType() {
    return entity.getClass();
  }

}
