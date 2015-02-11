package nl.knaw.huygens.timbuctoo.storage.neo4j;

import java.util.List;

import org.neo4j.graphdb.Node;

import com.google.common.collect.Lists;

public class EntityWrapper {

  private List<FieldWrapper> fieldWrappers;

  public EntityWrapper() {
    fieldWrappers = Lists.newArrayList();
  }

  public void addValuesToNode(Node node) {
    for (FieldWrapper fieldWrapper : fieldWrappers) {
      fieldWrapper.addValueToNode(node);
    }
  }

  public String addAdministrativeValues(Node node) {
    // TODO Auto-generated method stub
    return null;
  }

  public void addFieldWrapper(FieldWrapper fieldWrapper) {
    fieldWrappers.add(fieldWrapper);
  }

}
