package nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion;

import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType.ADMINISTRATIVE;

import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.PropertyContainerConverter;

import org.neo4j.graphdb.PropertyContainer;

import com.google.common.collect.Maps;

abstract class AbstractExtendablePropertyContainerConverter<U extends PropertyContainer, T extends Entity> implements ExtendablePropertyContainerConverter<U, T>, PropertyContainerConverter<U, T> {

  protected final Class<T> type;
  protected final Map<String, PropertyConverter> fieldNamePropertyConverterMap;
  protected final EntityInstantiator entityInstantiator;

  public AbstractExtendablePropertyContainerConverter(Class<T> type, EntityInstantiator entityInstantiator) {
    this.type = type;
    this.entityInstantiator = entityInstantiator;
    this.fieldNamePropertyConverterMap = Maps.newHashMap();
  }

  @Override
  public final void addValuesToPropertyContainer(U propertyContainer, T entity) throws ConversionException {
    executeCustomSerializationActions(propertyContainer, entity);
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      propertyConverter.setPropertyContainerProperty(propertyContainer, entity);
    }
  }

  private Collection<PropertyConverter> getPropertyConverters() {
    return fieldNamePropertyConverterMap.values();
  }

  protected abstract void executeCustomSerializationActions(U propertyContainer, T entity);

  @Override
  public final void addValuesToEntity(T entity, U propertyContainer) throws ConversionException {
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      propertyConverter.addValueToEntity(entity, propertyContainer);
    }
    executeCustomDeserializationActions(entity, propertyContainer);
  }

  @Override
  public void addPropertyConverter(PropertyConverter fieldWrapper) {
    mapToFieldName(fieldWrapper);
  }

  private void mapToFieldName(PropertyConverter propertyConverter) {
    fieldNamePropertyConverterMap.put(propertyConverter.getName(), propertyConverter);
  }

  @Override
  public final void updatePropertyContainer(U propertyContainer, T entity) throws ConversionException {
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      if (propertyConverter.getFieldType() != ADMINISTRATIVE) {
        propertyConverter.setPropertyContainerProperty(propertyContainer, entity);
      }
    }

  }

  protected abstract void executeCustomDeserializationActions(T entity, U propertyContainer);

  @Override
  public final void updateModifiedAndRev(U propertyContainer, T entity) throws ConversionException {
    getPropertyConverterByFieldName(MODIFIED_PROPERTY_NAME).setPropertyContainerProperty(propertyContainer, entity);
    getPropertyConverterByFieldName(REVISION_PROPERTY_NAME).setPropertyContainerProperty(propertyContainer, entity);
  }

  private PropertyConverter getPropertyConverterByFieldName(String fieldName) {
    return fieldNamePropertyConverterMap.get(fieldName);
  }

  @Override
  public final T convertToEntity(U propertyContainer) throws ConversionException, InstantiationException {
    T entity = entityInstantiator.createInstanceOf(type);

    addValuesToEntity(entity, propertyContainer);

    return entity;
  }

  @Override
  public final String getPropertyName(String fieldName) {
    if (!hasPropertyContainerForField(fieldName)) {
      throw new FieldNonExistingException(type, fieldName);
    }
    return getPropertyConverterByFieldName(fieldName).getPropertyName();
  }

  private boolean hasPropertyContainerForField(String fieldName) {
    return fieldNamePropertyConverterMap.containsKey(fieldName);
  }

}