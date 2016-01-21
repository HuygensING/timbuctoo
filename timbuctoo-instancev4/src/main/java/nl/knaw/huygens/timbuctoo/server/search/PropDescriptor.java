package nl.knaw.huygens.timbuctoo.server.search;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface PropDescriptor {
  String get(Vertex vertex);
}
