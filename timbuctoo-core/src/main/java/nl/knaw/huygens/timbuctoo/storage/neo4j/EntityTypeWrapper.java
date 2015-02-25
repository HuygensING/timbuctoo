package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import com.google.common.collect.Lists;

public class EntityTypeWrapper<T extends Entity> {

  private List<FieldWrapper> fieldWrappers;

  public EntityTypeWrapper() {
    fieldWrappers = Lists.newArrayList();
  }

  public void addValuesToNode(Node node, T entity) throws ConversionException {
    addName(node, entity);
    for (FieldWrapper fieldWrapper : fieldWrappers) {
      fieldWrapper.addValueToNode(node, entity);
    }
  }

  private void addName(Node node, T entity) {
    node.addLabel(DynamicLabel.label(TypeNames.getInternalName(entity.getClass())));
  }

  public void addValuesToEntity(T entity, Node node) throws ConversionException {
    for (FieldWrapper fieldWrapper : fieldWrappers) {
      fieldWrapper.addValueToEntity(entity, node);
    }
  }

  public void addFieldWrapper(FieldWrapper fieldWrapper) {
    fieldWrappers.add(fieldWrapper);
  }

}
