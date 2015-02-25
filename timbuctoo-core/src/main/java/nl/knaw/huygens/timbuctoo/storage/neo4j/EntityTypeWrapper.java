package nl.knaw.huygens.timbuctoo.storage.neo4j;

import static nl.knaw.huygens.timbuctoo.model.Entity.CREATED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.ID_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.MODIFIED_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class EntityTypeWrapper<T extends Entity> {

  private List<FieldWrapper> fieldWrappers;
  private String id;
  private Change modified;
  private Change created;
  private int revision;

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

  public void addAdministrativeValues(Node node) {
    node.setProperty(ID_PROPERTY_NAME, id);
    node.setProperty(REVISION_PROPERTY_NAME, revision);
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      node.setProperty(CREATED_PROPERTY_NAME, objectMapper.writeValueAsString(created));
      node.setProperty(MODIFIED_PROPERTY_NAME, objectMapper.writeValueAsString(modified));
    } catch (JsonProcessingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void addValuesToEntity(T entity, Node node) throws ConversionException {
    for (FieldWrapper fieldWrapper : fieldWrappers) {
      fieldWrapper.addValueToEntity(entity, node);
    }
  }

  public void addFieldWrapper(FieldWrapper fieldWrapper) {
    fieldWrappers.add(fieldWrapper);
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setModified(Change modified) {
    this.modified = modified;

  }

  public void setCreated(Change created) {
    this.created = created;
  }

  public void setRev(int revision) {
    this.revision = revision;
  }

  public String getId() {
    return id;
  }

}
