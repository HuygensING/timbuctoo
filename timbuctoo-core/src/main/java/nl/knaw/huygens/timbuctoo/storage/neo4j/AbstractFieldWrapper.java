package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.Node;

public abstract class AbstractFieldWrapper implements FieldWrapper {

  private Field field;
  private Class<? extends Entity> containingType;
  private FieldType fieldType;
  private String fieldName;

  /* (non-Javadoc)
   * @see nl.knaw.huygens.timbuctoo.storage.neo4j.FieldWrapper#setContainingType(java.lang.Class)
   */
  @Override
  public void setContainingType(Class<? extends Entity> containingType) {
    this.containingType = containingType;
  }

  /* (non-Javadoc)
   * @see nl.knaw.huygens.timbuctoo.storage.neo4j.FieldWrapper#setField(java.lang.reflect.Field)
   */
  @Override
  public void setField(Field field) {
    this.field = field;
  }

  protected Object getFieldValue(Entity entity) throws IllegalArgumentException, IllegalAccessException {
    field.setAccessible(true);
    return field.get(entity);
  }

  /* (non-Javadoc)
   * @see nl.knaw.huygens.timbuctoo.storage.neo4j.FieldWrapper#addValueToNode(org.neo4j.graphdb.Node, nl.knaw.huygens.timbuctoo.model.Entity)
   */
  @Override
  public final void addValueToNode(Node node, Entity entity) throws IllegalArgumentException, IllegalAccessException {
    Object fieldValue = getFieldValue(entity);
    if (fieldValue != null) {
      node.setProperty(getName(), getFormattedValue(fieldValue));
    }
  }

  /* (non-Javadoc)
   * @see nl.knaw.huygens.timbuctoo.storage.neo4j.FieldWrapper#addValueToEntity(nl.knaw.huygens.timbuctoo.model.Entity, org.neo4j.graphdb.Node)
   */
  @Override
  public final void addValueToEntity(Entity entity, Node node) throws IllegalArgumentException, IllegalAccessException {
    field.setAccessible(true);
    if (node.hasProperty(getName())) {
      field.set(entity, convertValue(node.getProperty(getName()), field.getType()));
    }
  }

  protected abstract Object convertValue(Object value, Class<?> fieldType);

  protected String getName() {
    return fieldType.propertyName(getContainingType(), fieldName);
  }

  private Class<? extends Entity> getContainingType() {
    return containingType;
  }

  /* (non-Javadoc)
   * @see nl.knaw.huygens.timbuctoo.storage.neo4j.FieldWrapper#setFieldType(nl.knaw.huygens.timbuctoo.storage.neo4j.FieldType)
   */
  @Override
  public void setFieldType(FieldType fieldType) {
    this.fieldType = fieldType;
  }

  /* (non-Javadoc)
   * @see nl.knaw.huygens.timbuctoo.storage.neo4j.FieldWrapper#setName(java.lang.String)
   */
  @Override
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
