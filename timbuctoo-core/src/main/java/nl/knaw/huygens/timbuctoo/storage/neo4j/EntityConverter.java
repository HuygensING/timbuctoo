package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.storage.neo4j.FieldType.ADMINISTRATIVE;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import com.google.common.collect.Lists;

public class EntityConverter<T extends Entity> {

  private List<FieldConverter> fieldConverters;
  private Class<T> type;

  public EntityConverter(Class<T> type) {
    this.type = type;
    fieldConverters = Lists.newArrayList();
  }

  public void addValuesToNode(Node node, T entity) throws ConversionException {
    addName(node);
    for (FieldConverter fieldWrapper : fieldConverters) {
      fieldWrapper.setNodeProperty(node, entity);
    }
  }

  private void addName(Node node) {
    node.addLabel(DynamicLabel.label(TypeNames.getInternalName(type)));
  }

  public void addValuesToEntity(T entity, Node node) throws ConversionException {
    for (FieldConverter fieldWrapper : fieldConverters) {
      fieldWrapper.addValueToEntity(entity, node);
    }
  }

  public void addFieldConverter(FieldConverter fieldWrapper) {
    fieldConverters.add(fieldWrapper);
  }

  /**
   * Updates the non administrative properties of the node.
   * @param node the node to update
   * @param entity the entity that contains the data.
   * @throws ConversionException when the fieldConverter throws one.
   */
  public void updateNode(Node node, Entity entity) throws ConversionException {
    for (FieldConverter fieldConverter : fieldConverters) {
      if (fieldConverter.getFieldType() != ADMINISTRATIVE) {
        fieldConverter.setNodeProperty(node, entity);
      }
    }

  }

  /**
   * Updates the modified and revision properties of the node.
   * @param node the node to update
   * @param entity the entity that contains the data to update.
   */
  public void updateModifiedAndRev(Node node, Entity entity) {
    // TODO Auto-generated method stub

  }

}
