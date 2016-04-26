package nl.knaw.huygens.timbuctoo.search.description.sort;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public interface SortFieldDescription {
  String getName();

  GraphTraversal<Object, Object> getTraversal();
}
