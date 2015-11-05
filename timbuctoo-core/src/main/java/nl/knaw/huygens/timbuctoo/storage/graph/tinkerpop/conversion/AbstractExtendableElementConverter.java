package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.conversion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.Element;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.graph.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.graph.FieldType;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.getTypes;

abstract class AbstractExtendableElementConverter<T extends Entity, E extends Element> implements ElementConverter<T, E> {

  private ObjectMapper objectMapper;
  private static Logger LOG = LoggerFactory.getLogger(ExtendableVertexConverter.class);
  protected final EntityInstantiator entityInstantiator;
  protected final Class<T> type;
  private Map<String, PropertyConverter> fieldNamePropertyConverterMap;

  public AbstractExtendableElementConverter(Class<T> type, Collection<PropertyConverter> propertyConverters, EntityInstantiator entityInstantiator) {
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

    Set<String> types = getTypesProperty(element);

    types.add(TypeNames.getInternalName(variationType));

    setTypesProperty(element, types);

  }

  protected final void setTypesProperty(E element, Collection<String> types) {
    try {
      element.setProperty(ELEMENT_TYPES, objectMapper.writeValueAsString(types));
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected final Set<String> getTypesProperty(E element) {
    Set<String> types = null;
    if (element.getProperty(ELEMENT_TYPES) != null) {
      types = getTypes(element);
    } else {
      types = Sets.newHashSet();
    }
    return types;
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

  @Override
  public <U extends T> U convertToSubType(Class<U> type, E element) throws ConversionException {
    try {
      U entity = entityInstantiator.createInstanceOf(type);

      addValuesToEntity(entity, element);

      executeCustomDeserializationActions(entity, element);

      return entity;

    } catch (InstantiationException e) {
      throw new ConversionException(e);
    }
  }

  protected abstract void executeCustomDeserializationActions(T entity, E element);

  protected <U extends T> void addValuesToEntity(U entity, E element) throws ConversionException {
    for (PropertyConverter propertyConverter : propertyConverters()) {
      propertyConverter.addValueToEntity(entity, element);
    }
  }

  protected Collection<PropertyConverter> propertyConverters() {
    return fieldNamePropertyConverterMap.values();
  }

  @Override
  public String getPropertyName(String fieldName) {
    verifyTypeContainsField(fieldName);

    return getPropertyConverterByFieldName(fieldName).completePropertyName();
  }

  protected final PropertyConverter getPropertyConverterByFieldName(String fieldName) {
    return fieldNamePropertyConverterMap.get(fieldName);
  }

  private boolean hasPropertyConverterForField(String fieldName) {
    return fieldNamePropertyConverterMap.containsKey(fieldName);
  }

  protected final void verifyTypeContainsField(String fieldName) {
    if (!hasPropertyConverterForField(fieldName)) {
      throw new NoSuchFieldException(type, fieldName);
    }
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

  @Override
  public void removePropertyByFieldName(E element, String fieldName) {
    verifyTypeContainsField(fieldName);

    getPropertyConverterByFieldName(fieldName).removeFrom(element);
  }


}
