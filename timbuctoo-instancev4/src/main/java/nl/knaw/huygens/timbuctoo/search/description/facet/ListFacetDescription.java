package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

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
  public Facet getFacet(List<Vertex> vertices) {

    Map<String, Long> counts = vertices.stream()
                                       .filter(vertex -> vertex.keys().contains(propertyName))
                                       .collect(
                                         groupingBy(vertex1 -> parser.parse(vertex1.value(propertyName)), counting()));

    List<Facet.Option> options =
      counts.entrySet().stream().map(entry -> new Facet.DefaultOption(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    return new Facet(facetName, options, "LIST");
  }
}
