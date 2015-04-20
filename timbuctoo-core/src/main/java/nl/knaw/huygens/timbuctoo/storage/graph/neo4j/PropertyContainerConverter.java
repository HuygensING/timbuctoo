package nl.knaw.huygens.timbuctoo.storage.graph.neo4j;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.PropertyContainer;

public interface PropertyContainerConverter<U extends PropertyContainer, T extends Entity> {

  void addValuesToPropertyContainer(U propertyContainer, T entity) throws ConversionException;

  void addValuesToEntity(T entity, U propertyContainer) throws ConversionException;

  /**
   * Updates the non administrative properties of the PropertyContainer.
   * @param propertyContainer the PropertyContainer to update
   * @param entity the entity that contains the data.
   * @throws ConversionException when the values of the Entity cannot be converted.
   */
  void updatePropertyContainer(U propertyContainer, T entity) throws ConversionException;

  /**
   * Updates the modified and revision properties of the PropertyContainer.
   * @param propertyContainer the PropertyContainer to update
   * @param entity the entity that contains the data to update.
   * @throws ConversionException when the values of the Entity cannot be converted. 
   */
  void updateModifiedAndRev(U propertyContainer, T entity) throws ConversionException;

  T convertToEntity(U propertyContainer) throws ConversionException, InstantiationException;

  String getPropertyName(String fieldName);

}
