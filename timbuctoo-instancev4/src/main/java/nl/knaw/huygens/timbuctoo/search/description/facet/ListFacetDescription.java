package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.ListFacetValue;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
      counts.entrySet().stream().map(entry -> new Facet.DefaultOption(parser.parse(entry.getKey()), entry.getValue()))
            .collect(toList());

    return new Facet(facetName, options, "LIST");
  }

  @Override
  @SuppressWarnings("unchecked")
  public void filter(GraphTraversal<Vertex, Vertex> graphTraversal, List<FacetValue> facets) {
    Optional<FacetValue> facetValue =
      facets.stream().filter(facet -> Objects.equals(facet.getName(), facetName)).findFirst();

    if (facetValue.isPresent()) {
      FacetValue value = facetValue.get();
      if (value instanceof ListFacetValue) {

        List<String> values = ((ListFacetValue) value).getValues();
        if (!values.isEmpty()) {
          graphTraversal.where(__.has(propertyName, P.test((o1, o2) -> {
            List<String> possibileValues = (List<String>) o2;
            return possibileValues.contains(parser.parse("" + o1));
          }, values)));
        }
      }
    }
  }
}
