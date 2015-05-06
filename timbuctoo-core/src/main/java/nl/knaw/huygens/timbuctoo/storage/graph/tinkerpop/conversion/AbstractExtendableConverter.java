package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getTypes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.neo4j.conversion.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Element;

abstract class AbstractExtendableConverter<T extends Entity, E extends Element> implements ElementConverter<T, E> {

  private ObjectMapper objectMapper;
  private static Logger LOG = LoggerFactory.getLogger(ExtendableVertexConverter.class);
  protected final EntityInstantiator entityInstantiator;
  protected final Class<T> type;
  private Map<String, PropertyConverter> fieldNamePropertyConverterMap;

  public AbstractExtendableConverter(Class<T> type, Collection<PropertyConverter> propertyConverters, EntityInstantiator entityInstantiator) {
    this.type = type;
    this.entityInstantiator = entityInstantiator;
    mapPropertyConverters(propertyConverters);
    objectMapper = new ObjectMapper();
  }

  protected void mapPropertyConverters(Collection<PropertyConverter> propertyConverters) {
    fieldNamePropertyConverterMap = Maps.newHashMap();
    for (PropertyConverter propertyConverter : propertyConverters) {
      fieldNamePropertyConverterMap.put(propertyConverter.getFieldName(), propertyConverter);
    }
  }

  @Override
  public void addValuesToElement(E element, T entity) throws ConversionException {
    addVariation(element, type);
    for (PropertyConverter propertyConverter : propertyConverters()) {
      propertyConverter.setPropertyOfElement(element, entity);
    }
  }

  private void addVariation(E element, Class<? extends Entity> variationType) {
    LOG.debug("add variation \"{}\"", variationType);

    List<String> types = null;
    if (element.getProperty(ELEMENT_TYPES) != null) {
      types = getTypes(element);
    } else {
      types = Lists.newArrayList();
    }

    types.add(TypeNames.getInternalName(variationType));

    try {
      element.setProperty(ELEMENT_TYPES, objectMapper.writeValueAsString(types));
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }

  }

  @Override
  public T convertToEntity(E element) throws ConversionException {
    try {
      T entity = entityInstantiator.createInstanceOf(type);

      addValuesToEntity(entity, element);

      executeCustomDeserializationActions(entity, element);

      return entity;

    } catch (InstantiationException e) {
      throw new ConversionException("Entity could not be instantiated.");
    }

  }

  protected abstract void executeCustomDeserializationActions(T entity, E element);

  protected <U extends T> void addValuesToEntity(U entity, E element) throws ConversionException {
    for (PropertyConverter propertyConverter : propertyConverters()) {
      propertyConverter.addValueToEntity(entity, element);
    }
  }

  private Collection<PropertyConverter> propertyConverters() {
    return fieldNamePropertyConverterMap.values();
  }

  @Override
  public String getPropertyName(String fieldName) {
    if (!hasPropertyContainerForField(fieldName)) {
      throw new FieldNonExistingException(type, fieldName);
    }
    return getPropertyConverterByFieldName(fieldName).propertyName();
  }

  private PropertyConverter getPropertyConverterByFieldName(String fieldName) {
    return fieldNamePropertyConverterMap.get(fieldName);
  }

  private boolean hasPropertyContainerForField(String fieldName) {
    return fieldNamePropertyConverterMap.containsKey(fieldName);
  }

  @Override
  public void updateModifiedAndRev(E elementMock, Entity entity) throws ConversionException {
    getPropertyConverterByFieldName(MODIFIED_PROPERTY_NAME).setPropertyOfElement(elementMock, entity);
    getPropertyConverterByFieldName(REVISION_PROPERTY_NAME).setPropertyOfElement(elementMock, entity);
  }

  @Override
  public void updateElement(E element, Entity entity) throws ConversionException {
    for (PropertyConverter propertyConverter : propertyConverters()) {

      if (propertyConverter.getFieldType() != FieldType.ADMINISTRATIVE) {
        propertyConverter.setPropertyOfElement(element, entity);
      }
    }
  }

}