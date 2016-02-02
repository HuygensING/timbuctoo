package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class ListFacetDescription implements FacetDescription {
  private final String facetName;
  private final String propertyName;
  private final PropertyParser parser;

  public ListFacetDescription(String facetName, String propertyName, PropertyParser parser) {
    this.facetName = facetName;
    this.propertyName = propertyName;
    this.parser = parser;
  }

  @Override
  public Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult) {

    Map<String, Long> counts =
      searchResult.has(propertyName).<String>groupCount().by(propertyName).next();

    List<Facet.Option> options =
      counts.entrySet().stream().map(entry -> new Facet.DefaultOption(entry.getKey(), entry.getValue()))
            .collect(toList());

    return new Facet(facetName, options, "LIST");
  }
}
