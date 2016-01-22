package nl.knaw.huygens.timbuctoo.server.search;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public class SinglePropDescriptor implements PropDescriptor {

  private final PropertyParser parser;
  private final PropertyGetter getter;

  public SinglePropDescriptor(PropertyGetter getter, PropertyParser parser) {
    this.getter = getter;
    this.parser = parser;
  }

  @Override
  public String get(Vertex vertex) {
    return parser.parse(getter.get(vertex));
  }
}
