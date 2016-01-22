package nl.knaw.huygens.timbuctoo.server.search.property;

import nl.knaw.huygens.timbuctoo.server.search.PropDescriptor;
import nl.knaw.huygens.timbuctoo.server.search.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Vertex;

class LocalPropertyDescriptor implements PropDescriptor {
  private final String propertyName;
  private final PropertyParser parser;

  public LocalPropertyDescriptor(String propertyName, PropertyParser parser) {
    this.propertyName = propertyName;
    this.parser = parser;
  }

  @Override
  public String get(Vertex vertex) {
    if (vertex.keys().contains(propertyName)) {
      return parser.parse(vertex.value(propertyName));
    }

    return null;
  }
}
