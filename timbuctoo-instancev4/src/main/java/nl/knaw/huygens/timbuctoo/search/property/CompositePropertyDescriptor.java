package nl.knaw.huygens.timbuctoo.search.property;

import nl.knaw.huygens.timbuctoo.search.PropertyDescriptor;
import org.apache.tinkerpop.gremlin.structure.Vertex;

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
    if (value == null) {
      value = backUpDescriptor.get(vertex);
    }
    return value;

  }
}

