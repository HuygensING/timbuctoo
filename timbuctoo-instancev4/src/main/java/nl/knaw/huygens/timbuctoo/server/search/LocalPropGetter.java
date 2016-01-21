package nl.knaw.huygens.timbuctoo.server.search;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public class LocalPropGetter implements PropGetter {

  private String propName;

  public LocalPropGetter(String propName) {
    this.propName = propName;
  }

  @Override
  public String get(Vertex vertex) {

    return vertex.keys().contains(propName) ?
      vertex.value(propName) :
      null;
  }
}
