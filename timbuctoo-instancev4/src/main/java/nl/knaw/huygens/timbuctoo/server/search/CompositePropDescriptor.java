package nl.knaw.huygens.timbuctoo.server.search;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public class CompositePropDescriptor implements PropDescriptor {

  private final PropDescriptor preferredDescriptor;
  private final PropDescriptor backUpDescriptor;

  public CompositePropDescriptor(PropDescriptor preferredDescriptor, PropDescriptor backUpDescriptor) {
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

