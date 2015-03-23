package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.property;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.neo4j.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.FieldType;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.PropertyConverter;

import org.neo4j.graphdb.PropertyContainer;

abstract class AbstractPropertyConverter implements PropertyConverter {

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
  public final void setPropertyContainerProperty(PropertyContainer propertyContainer, Entity entity) throws ConversionException {
    try {
      Object fieldValue = getFieldValue(entity);

      if (shouldAddValue(fieldValue)) {
        propertyContainer.setProperty(getPropertyName(), getFormattedValue(fieldValue));
      }
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new ConversionException(e);
    }

  }

  protected boolean shouldAddValue(Object fieldValue) {
    return fieldValue != null;
  }

  /* (non-Javadoc)
   * @see nl.knaw.huygens.timbuctoo.storage.neo4j.FieldWrapper#addValueToEntity(nl.knaw.huygens.timbuctoo.model.Entity, org.neo4j.graphdb.Node)
   */
  @Override
  public final void addValueToEntity(Entity entity, PropertyContainer propertyContainer) throws ConversionException {
    try {
      field.setAccessible(true);
      if (propertyContainer.hasProperty(getPropertyName())) {
        fillField(entity, propertyContainer);
      }
    } catch (IllegalAccessException | IllegalArgumentException e) {
      throw new ConversionException(e);
    }
  }

  protected void fillField(Entity entity, PropertyContainer propertyContainer) throws IllegalArgumentException, IllegalAccessException, ConversionException {
    field.set(entity, getValue(propertyContainer));
  }

  protected abstract Object convertValue(Object value, Class<?> fieldType) throws IllegalArgumentException;

  protected String getPropertyName() {
    return fieldType.propertyName(getContainingType(), fieldName);
  }

  @Override
  public String getName() {
    return fieldName;
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

  @Override
  public FieldType getFieldType() {
    return fieldType;
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

  protected Object getValue(PropertyContainer propertyContainer) throws ConversionException {
    if (propertyContainer.hasProperty(getPropertyName())) {
      try {
        return convertValue(propertyContainer.getProperty(getPropertyName()), getType());
      } catch (IllegalArgumentException e) {
        throw new ConversionException(e);
      }
    }
    return null;
  }

  private Class<?> getType() {
    return field.getType();
  }
}
