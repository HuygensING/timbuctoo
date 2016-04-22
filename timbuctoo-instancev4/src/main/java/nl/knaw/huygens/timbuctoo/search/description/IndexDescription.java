package nl.knaw.huygens.timbuctoo.search.description;


import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Set;

public interface IndexDescription {

  Set<String> getSortIndexPropertyNames();

  void addIndexedSortProperties(Vertex vertex);
}
