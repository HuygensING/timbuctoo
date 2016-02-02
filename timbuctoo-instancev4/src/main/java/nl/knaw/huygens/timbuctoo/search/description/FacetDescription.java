package nl.knaw.huygens.timbuctoo.search.description;

import nl.knaw.huygens.timbuctoo.search.description.facet.Facet;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface FacetDescription {
  Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult);
}
