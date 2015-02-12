package nl.knaw.huygens.timbuctoo.storage.neo4j;

import org.neo4j.graphdb.Node;

/**
 * A FieldWrapper that wraps fields for primitives, primitive wrappers, strings, 
 * or collections of the former.  
 *
 */
public class SimpleValueFieldWrapper extends FieldWrapper {

  @Override
  public void addValueToNode(Node node) throws IllegalArgumentException, IllegalAccessException {
    node.setProperty(getName(), getFieldValue());
  }

}
