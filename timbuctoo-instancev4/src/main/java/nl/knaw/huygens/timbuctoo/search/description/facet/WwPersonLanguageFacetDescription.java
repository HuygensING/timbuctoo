package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

class WwPersonLanguageFacetDescription implements FacetDescription {

  private final String facetName;

  public WwPersonLanguageFacetDescription(String facetName) {
    this.facetName = facetName;
  }

  @Override
  public Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult) {
    /*
     * Do not use "in()" instead of "inE().outV()" or "out()" instead of "outE().inV()",
     * because Neo4j will throw an exception when used in the query below.
     */
    Map<String, Long> languageCounts =
      searchResult.as("a").inE("isCreatedBy").outV().outE("hasWorkLanguage").inV().as("b").dedup("a", "b")
                  .has("wwlanguage_name").<String>groupCount()
        .by("wwlanguage_name").next();

    List<Facet.Option> options = languageCounts.entrySet().stream()
                                               .map(count -> new Facet.DefaultOption(count.getKey(), count.getValue()))
                                               .collect(toList());

    return new Facet(facetName, options, "LIST");
  }
}
