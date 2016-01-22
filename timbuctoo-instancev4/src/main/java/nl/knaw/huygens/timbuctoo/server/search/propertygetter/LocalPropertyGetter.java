package nl.knaw.huygens.timbuctoo.server.search.propertygetter;

import nl.knaw.huygens.timbuctoo.server.search.PropertyGetter;
import org.apache.tinkerpop.gremlin.structure.Vertex;

class LocalPropertyGetter implements PropertyGetter {

  private String propName;

  public LocalPropertyGetter(String propName) {
    this.propName = propName;
  }

  @Override
  public String get(Vertex vertex) {

    return vertex.keys().contains(propName) ?
      vertex.value(propName) :
      null;
  }
}
