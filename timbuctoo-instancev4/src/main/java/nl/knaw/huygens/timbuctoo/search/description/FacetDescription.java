package nl.knaw.huygens.timbuctoo.search.description;

import nl.knaw.huygens.timbuctoo.search.description.facet.Facet;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

public interface FacetDescription {
  Facet getFacet(List<Vertex> vertices);
}
