package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
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

class WwPersonLanguageFacetDescription implements FacetDescription {

  private final String facetName;

  public WwPersonLanguageFacetDescription(String facetName) {
    this.facetName = facetName;
  }

  @Override
  public Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult) {
    /*
     * Do not use "in()" instead of "inE().outV()" or "out()" instead of "outE().inV()",
     * because the Neo4jGraph will remove the label "b". This should be fixed in version 3.1.2.
     */
    Map<String, Long> languageCounts =
      searchResult.as("a").inE("isCreatedBy").outV().outE("hasWorkLanguage").inV().has("wwlanguage_name").as("b")
                  .dedup("a", "b").<String>groupCount().by("wwlanguage_name").next();

    List<Facet.Option> options = languageCounts.entrySet().stream()
                                               .map(count -> new Facet.DefaultOption(count.getKey(), count.getValue()))
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

    graphTraversal.where(__.inE("isCreatedBy").outV()
                           .outE("hasWorkLanguage").inV()
                           .has("wwlanguage_name", within(values)));

  }
}
