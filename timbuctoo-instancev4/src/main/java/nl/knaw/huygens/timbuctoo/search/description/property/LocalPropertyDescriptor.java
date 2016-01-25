package nl.knaw.huygens.timbuctoo.search.description.property;

import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Vertex;

class LocalPropertyDescriptor implements PropertyDescriptor {
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
