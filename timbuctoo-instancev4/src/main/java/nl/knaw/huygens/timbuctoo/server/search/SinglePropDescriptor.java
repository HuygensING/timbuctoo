package nl.knaw.huygens.timbuctoo.server.search;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public class SinglePropDescriptor implements PropDescriptor {

  private final PropParser parser;
  private final PropGetter getter;

  public SinglePropDescriptor(PropGetter getter, PropParser parser) {
    this.getter = getter;
    this.parser = parser;
  }

  @Override
  public String get(Vertex vertex) {
    return parser.parse(getter.get(vertex));
  }
}
