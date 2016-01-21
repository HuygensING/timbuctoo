package nl.knaw.huygens.timbuctoo.server.search;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public class CompositePropDescriptor implements PropDescriptor {

  private final PropDescriptor propDescriptor1;
  private final PropDescriptor propDescriptor2;

  public CompositePropDescriptor(PropDescriptor propDescriptor1, PropDescriptor propDescriptor2) {
    this.propDescriptor1 = propDescriptor1;
    this.propDescriptor2 = propDescriptor2;
  }

  @Override
  public String get(Vertex vertex) {
    String value = propDescriptor1.get(vertex);
    if (value == null) {
      value = propDescriptor2.get(vertex);
    }
    return value;

  }
}

