package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.Node;

public abstract class FieldWrapper {

  private Field field;
  private Class<? extends Entity> containingType;
  private FieldType fieldType;
  private String fieldName;

  public void setContainingType(Class<? extends Entity> containingType) {
    this.containingType = containingType;
  }

  public void setField(Field field) {
    this.field = field;
  }

  protected Object getFieldValue(Entity entity) throws IllegalArgumentException, IllegalAccessException {
    field.setAccessible(true);
    return field.get(entity);
  }

  public final void addValueToNode(Node node, Entity entity) throws IllegalArgumentException, IllegalAccessException {
    Object fieldValue = getFieldValue(entity);
    if (fieldValue != null) {
      node.setProperty(getName(), getFormattedValue(fieldValue));
    }
  }

  /**
   * Extracts the value from the node and converts it so it can be added to the entity.
   * @param entity the entity to add the values to
   * @param node the node to retrieve the values from
   */
  public void addValueToEntity(Entity entity, Node node) {
    // TODO Auto-generated method stub

  }

  protected String getName() {
    return fieldType.propertyName(getContainingType(), fieldName);
  }

  private Class<? extends Entity> getContainingType() {
    return containingType;
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
