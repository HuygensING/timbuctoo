package nl.knaw.huygens.timbuctoo.search.description;

import nl.knaw.huygens.timbuctoo.server.rest.search.FullTextSearchParameter;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface FullTextSearchDescription {
  String getName();

  void filter(GraphTraversal<Vertex, Vertex> traversal, FullTextSearchParameter fullTextSearchParameter);
}
