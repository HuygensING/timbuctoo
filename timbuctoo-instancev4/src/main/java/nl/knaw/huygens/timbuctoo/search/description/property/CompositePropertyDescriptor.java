package nl.knaw.huygens.timbuctoo.search.description.property;

import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.helpers.Strings;

class CompositePropertyDescriptor implements PropertyDescriptor {

  private final PropertyDescriptor preferredDescriptor;
  private final PropertyDescriptor backUpDescriptor;

  public CompositePropertyDescriptor(PropertyDescriptor preferredDescriptor, PropertyDescriptor backUpDescriptor) {
    this.preferredDescriptor = preferredDescriptor;
    this.backUpDescriptor = backUpDescriptor;
  }

  @Override
  public String get(Vertex vertex) {
    String value = preferredDescriptor.get(vertex);
    if (Strings.isBlank(value)) {
      value = backUpDescriptor.get(vertex);
    }
    return value;

  }
}

