package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.ListFacetValue;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.apache.tinkerpop.gremlin.process.traversal.P.within;

public class DerivedListFacetDescription implements FacetDescription {
  private final String facetName;
  private final String propertyName;
  private final PropertyParser parser;
  private final String[] relations;
  private final String relationName;


  public DerivedListFacetDescription(String facetName, String propertyName, String relationName,
                                     PropertyParser parser, String... relations) {
    this.facetName = facetName;
    this.propertyName = propertyName;
    this.parser = parser;
    this.relations = relations;
    this.relationName = relationName;
  }

  @Override
  public Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult) {
    Map<String, Long> counts =
            searchResult.as("a").bothE(relations).otherV().bothE(relationName).inV().has(propertyName).as("b")
                    .dedup("a", "b").<String>groupCount().by(propertyName).next();

    List<Facet.Option> options = counts.entrySet().stream()
            .map(count -> new Facet.DefaultOption(parser.parse(count.getKey()), count.getValue()))
            .collect(toList());

    return new Facet(facetName, options, "LIST");
  }

  @Override
  public void filter(GraphTraversal<Vertex, Vertex> graphTraversal, List<FacetValue> facets) {
    Optional<FacetValue> first = facets.stream()
            .filter(facetValue -> Objects.equals(facetValue.getName(), facetName))
            .findFirst();

    if (!first.isPresent()) {
      return;
    }

    FacetValue facetValue = first.get();
    if (!(facetValue instanceof ListFacetValue)) {
      return;
    }

    List<String> values = ((ListFacetValue) facetValue).getValues();
    if (values.isEmpty()) {
      return;
    }

    graphTraversal.where(__.bothE(relations).otherV()
            .bothE(relationName).otherV()
            .values(propertyName)
            .map(value -> parser.parse((String) value.get()))
            .is(within(values)));
  }
}