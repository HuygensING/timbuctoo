package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.neo4j.FieldType.ADMINISTRATIVE;

import java.util.Collection;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import com.google.common.collect.Maps;

public class RegularEntityConverter<T extends Entity, U extends Node> implements EntityConverter<T, U> {

  private Class<T> type;
  private Map<String, FieldConverter> nameFieldConverterMap;

  public RegularEntityConverter(Class<T> type) {
    this.type = type;
    nameFieldConverterMap = Maps.newHashMap();
  }

  @Override
  public void addValuesToPropertyContainer(U node, T entity) throws ConversionException {
    addName(node);
    for (FieldConverter fieldConverter : getFieldConverters()) {
      fieldConverter.setPropertyContainerProperty(node, entity);
    }
  }

  private Collection<FieldConverter> getFieldConverters() {
    return nameFieldConverterMap.values();
  }

  private void addName(U node) {
    node.addLabel(DynamicLabel.label(TypeNames.getInternalName(type)));
  }

  @Override
  public void addValuesToEntity(T entity, U node) throws ConversionException {
    for (FieldConverter fieldConverter : getFieldConverters()) {
      fieldConverter.addValueToEntity(entity, node);
    }
  }

  @Override
  public void addFieldConverter(FieldConverter fieldWrapper) {
    mapToFieldName(fieldWrapper);
  }

  private void mapToFieldName(FieldConverter fieldConverter) {
    nameFieldConverterMap.put(fieldConverter.getName(), fieldConverter);
  }

  /**
   * Updates the non administrative properties of the node.
   * @param node the node to update
   * @param entity the entity that contains the data.
   * @throws ConversionException when the fieldConverter throws one.
   */
  @Override
  public void updatePropertyContainer(U node, Entity entity) throws ConversionException {
    for (FieldConverter fieldConverter : getFieldConverters()) {
      if (fieldConverter.getFieldType() != ADMINISTRATIVE) {
        fieldConverter.setPropertyContainerProperty(node, entity);
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
  public void updateModifiedAndRev(U node, Entity entity) throws ConversionException {
    getFieldConverterByName(MODIFIED_PROPERTY_NAME).setPropertyContainerProperty(node, entity);
    getFieldConverterByName(REVISION_PROPERTY_NAME).setPropertyContainerProperty(node, entity);
  }

  private FieldConverter getFieldConverterByName(String fieldName) {
    return nameFieldConverterMap.get(fieldName);
  }

}
