package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion;

import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.FieldType.ADMINISTRATIVE;

import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.neo4j.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.EntityInstantiator;
import nl.knaw.huygens.timbuctoo.storage.neo4j.NodeConverter;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import com.google.common.collect.Maps;

class ExtendableNodeConverter<T extends Entity> implements NodeConverter<T>, ExtendablePropertyContainerConverter<Node, T> {

  private final Class<T> type;
  private final Map<String, PropertyConverter> fieldNamePropertyConverterMap;
  private final EntityInstantiator entityInstantiator;

  public ExtendableNodeConverter(Class<T> type) {
    this(type, new EntityInstantiator());
  }

  public ExtendableNodeConverter(Class<T> type, EntityInstantiator entityInstantiator) {
    this.type = type;
    this.entityInstantiator = entityInstantiator;
    fieldNamePropertyConverterMap = Maps.newHashMap();
  }

  @Override
  public void addValuesToPropertyContainer(Node node, T entity) throws ConversionException {
    addName(node);
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      propertyConverter.setPropertyContainerProperty(node, entity);
    }
  }

  private Collection<PropertyConverter> getPropertyConverters() {
    return fieldNamePropertyConverterMap.values();
  }

  private void addName(Node node) {
    node.addLabel(DynamicLabel.label(TypeNames.getInternalName(type)));
  }

  @Override
  public void addValuesToEntity(T entity, Node node) throws ConversionException {
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      propertyConverter.addValueToEntity(entity, node);
    }
  }

  @Override
  public void addPropertyConverter(PropertyConverter fieldWrapper) {
    mapToFieldName(fieldWrapper);
  }

  private void mapToFieldName(PropertyConverter propertyConverter) {
    fieldNamePropertyConverterMap.put(propertyConverter.getName(), propertyConverter);
  }

  @Override
  public void updatePropertyContainer(Node node, Entity entity) throws ConversionException {
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      if (propertyConverter.getFieldType() != ADMINISTRATIVE) {
        propertyConverter.setPropertyContainerProperty(node, entity);
      }
    }

  }

  @Override
  public void updateModifiedAndRev(Node node, Entity entity) throws ConversionException {
    getPropertyConverterByFieldName(MODIFIED_PROPERTY_NAME).setPropertyContainerProperty(node, entity);
    getPropertyConverterByFieldName(REVISION_PROPERTY_NAME).setPropertyContainerProperty(node, entity);
  }

  private PropertyConverter getPropertyConverterByFieldName(String fieldName) {
    return fieldNamePropertyConverterMap.get(fieldName);
  }

  @Override
  public T convertToEntity(Node propertyContainer) throws ConversionException, InstantiationException {
    T entity = entityInstantiator.createInstanceOf(type);

    addValuesToEntity(entity, propertyContainer);

    return entity;
  }

  @Override
  public String getPropertyName(String fieldName) {
    if (!hasPropertyContainerForField(fieldName)) {
      throw new FieldNonExistingException(type, fieldName);
    }
    return getPropertyConverterByFieldName(fieldName).getPropertyName();
  }

  private boolean hasPropertyContainerForField(String fieldName) {
    return fieldNamePropertyConverterMap.containsKey(fieldName);
  }
}
