package nl.knaw.huygens.timbuctoo.search;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface PropertyDescriptor {
  String get(Vertex vertex);
}
