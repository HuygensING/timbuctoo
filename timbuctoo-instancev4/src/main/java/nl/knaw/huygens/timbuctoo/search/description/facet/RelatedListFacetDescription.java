package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.ListFacetValue;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * A facet description that creates a "LIST" facet with properties from connected vertices.
 */
public class RelatedListFacetDescription implements FacetDescription {
  private final String facetName;
  private final String propertyName;
  private final PropertyParser parser;
  private final String[] relations;

  public RelatedListFacetDescription(String facetName, String propertyName, PropertyParser parser,
                                     String... relations) {
    this.facetName = facetName;
    this.propertyName = propertyName;
    this.parser = parser;
    this.relations = relations;
  }

  @Override
  public String getName() {
    return facetName;
  }

  @Override
  public Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult) {
    Map<String, Set<Vertex>> grouped = new HashMap<>();

    searchResult.as("source").bothE(relations).otherV().has(propertyName).as("target").dedup("source", "target")
            .select("source", "target").forEachRemaining(map -> {
              Vertex source = (Vertex) map.get("source");
              String targetValue = (String) ((Vertex) map.get("target")).property(propertyName).value();
              if (!grouped.containsKey(targetValue)) {
                grouped.put(targetValue, new HashSet<>());
              }
              grouped.get(targetValue).add(source);
            });

    List<Facet.Option> options  = grouped.entrySet().stream().map(group ->
            new Facet.DefaultOption(parser.parse(group.getKey()), group.getValue().size())
        ).filter(facetOption -> facetOption.getName() != null).collect(toList());

    return new Facet(facetName, options, "LIST");
  }

  @Override
  public void filter(GraphTraversal<Vertex, Vertex> graphTraversal, List<FacetValue> facets) {
    Optional<FacetValue> value = facets.stream()
                                       .filter(facetValue -> Objects.equals(facetValue.getName(), facetName))
                                       .findFirst();

    if (value.isPresent()) {
      FacetValue facetValue = value.get();
      if (facetValue instanceof ListFacetValue) {
        List<String> values = ((ListFacetValue) facetValue).getValues();
        if (!values.isEmpty()) {
          graphTraversal.where(__.bothE(relations).otherV().has(propertyName, P.test((o1, o2) -> {
            List<String> possibleValues = (List<String>) o2;
            return possibleValues.contains(parser.parse("" + o1));
          }, values)))  ;
        }
      }
    }
  }

  @Override
  public List<String> getValues(Vertex vertex) {
    List<String> result = new ArrayList<>();
    vertex.vertices(Direction.BOTH, relations).forEachRemaining(targetVertex -> {
      if(targetVertex.property(propertyName).isPresent()) {
        result.add((String) targetVertex.property(propertyName).value());
      }
    });
    return result;
  }
}
