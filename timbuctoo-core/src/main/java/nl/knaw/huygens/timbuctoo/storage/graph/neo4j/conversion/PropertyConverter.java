package nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion;

import java.lang.reflect.Field;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;

import org.neo4j.graphdb.PropertyContainer;

public interface PropertyConverter {

  void setContainingType(Class<? extends Entity> containingType);

  void setField(Field field);

  void setPropertyContainerProperty(PropertyContainer propertyContainer, Entity entity) throws ConversionException;

  /**
   * Extracts the value from the propertyContainer and converts it so it can be added to the entity.
   * @param entity the entity to add the values to
   * @param propertyContainer the propertyContainer to retrieve the values from
   * @throws ConversionException when the value in the node could not be added to the entity.
   */
  void addValueToEntity(Entity entity, PropertyContainer propertyContainer) throws ConversionException;

  void setFieldType(FieldType fieldType);

  void setName(String fieldName);

  FieldType getFieldType();

  String getName();

  String getPropertyName();

}