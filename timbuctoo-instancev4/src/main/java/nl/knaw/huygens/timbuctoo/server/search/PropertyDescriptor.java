package nl.knaw.huygens.timbuctoo.server.search;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface PropertyDescriptor {
  String get(Vertex vertex);
}
