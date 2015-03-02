package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import com.google.common.collect.Lists;

public class EntityConverter<T extends Entity> {

  private List<FieldConverter> fieldWrappers;
  private Class<T> type;

  public EntityConverter(Class<T> type) {
    this.type = type;
    fieldWrappers = Lists.newArrayList();
  }

  public void addValuesToNode(Node node, T entity) throws ConversionException {
    addName(node);
    for (FieldConverter fieldWrapper : fieldWrappers) {
      fieldWrapper.addValueToNode(node, entity);
    }
  }

  private void addName(Node node) {
    node.addLabel(DynamicLabel.label(TypeNames.getInternalName(type)));
  }

  public void addValuesToEntity(T entity, Node node) throws ConversionException {
    for (FieldConverter fieldWrapper : fieldWrappers) {
      fieldWrapper.addValueToEntity(entity, node);
    }
  }

  public void addFieldWrapper(FieldConverter fieldWrapper) {
    fieldWrappers.add(fieldWrapper);
  }

  /**
   * Updates the non administrative properties of the node.
   * @param node the node to update
   * @param entity the entity that contains the data.
   */
  public void updateNode(Node node, Entity entity) {
    // TODO Auto-generated method stub

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
