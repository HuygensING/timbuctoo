package nl.knaw.huygens.timbuctoo.storage.neo4j.conversion;

import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.FieldType.ADMINISTRATIVE;

import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.neo4j.ConversionException;
import nl.knaw.huygens.timbuctoo.storage.neo4j.NodeConverter;
import nl.knaw.huygens.timbuctoo.storage.neo4j.conversion.property.PropertyConverter;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import com.google.common.collect.Maps;

class ExtendableNodeConverter<T extends Entity> implements NodeConverter<T>, ExtendablePropertyContainerConverter<Node, T> {

  private Class<T> type;
  private Map<String, PropertyConverter> namePropertyConverterMap;

  public ExtendableNodeConverter(Class<T> type) {
    this.type = type;
    namePropertyConverterMap = Maps.newHashMap();
  }

  @Override
  public void addValuesToPropertyContainer(Node node, T entity) throws ConversionException {
    addName(node);
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      propertyConverter.setPropertyContainerProperty(node, entity);
    }
  }

  private Collection<PropertyConverter> getPropertyConverters() {
    return namePropertyConverterMap.values();
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
    namePropertyConverterMap.put(propertyConverter.getName(), propertyConverter);
  }

  /**
   * Updates the non administrative properties of the node.
   * @param node the node to update
   * @param entity the entity that contains the data.
   * @throws ConversionException when the fieldConverter throws one.
   */
  @Override
  public void updatePropertyContainer(Node node, Entity entity) throws ConversionException {
    for (PropertyConverter propertyConverter : getPropertyConverters()) {
      if (propertyConverter.getFieldType() != ADMINISTRATIVE) {
        propertyConverter.setPropertyContainerProperty(node, entity);
      }
    }

  }

  /**
   * Updates the modified and revision properties of the node.
   * @param node the node to update
   * @param entity the entity that contains the data to update.
   * @throws ConversionException when one of the FieldConverters throws one 
   */
  @Override
  public void updateModifiedAndRev(Node node, Entity entity) throws ConversionException {
    getPropertyConverterByName(MODIFIED_PROPERTY_NAME).setPropertyContainerProperty(node, entity);
    getPropertyConverterByName(REVISION_PROPERTY_NAME).setPropertyContainerProperty(node, entity);
  }

  private PropertyConverter getPropertyConverterByName(String fieldName) {
    return namePropertyConverterMap.get(fieldName);
  }

}
