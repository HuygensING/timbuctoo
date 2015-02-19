package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.neo4j.graphdb.Node;

public abstract class FieldWrapper {

  private Field field;
  private SystemEntity entity;
  private FieldType fieldType;
  private String fieldName;

  public void setContainingEntity(SystemEntity entity) {
    this.entity = entity;
  }

  public void setField(Field field) {
    this.field = field;
  }

  protected Object getFieldValue() throws IllegalArgumentException, IllegalAccessException {
    field.setAccessible(true);
    return field.get(entity);
  }

  public final void addValueToNode(Node node) throws IllegalArgumentException, IllegalAccessException {
    Object fieldValue = getFieldValue();
    if (fieldValue != null) {
      node.setProperty(getName(), getFormattedValue(fieldValue));
    }
  }

  protected String getName() {
    return fieldType.propertyName(getContainingType(), fieldName);
  }

  private Class<? extends SystemEntity> getContainingType() {
    return entity.getClass();
  }

  public void setFieldType(FieldType fieldType) {
    this.fieldType = fieldType;
  }

  public void setName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Formats the value so it is usable as value of a Neo4J node.
   * @param fieldValue the unformatted value
   * @return the formatted value
   * @throws IllegalArgumentException if the value cannot be formatted.
   */
  protected abstract Object getFormattedValue(Object fieldValue) throws IllegalArgumentException;

}
