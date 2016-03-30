package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.ListFacetValue;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A facet description that creates a "LIST" facet with multi-valued properties from connected vertices.
 */
public class RelatedMultiValueListFacetDescription implements FacetDescription {
  public static final Logger LOG = LoggerFactory.getLogger(MultiValueListFacetDescription.class);

  private final String facetName;
  private final String propertyName;
  private final String[] relations;
  private final ObjectMapper mapper;


  public RelatedMultiValueListFacetDescription(String facetName, String propertyName, String... relations) {
    this.facetName = facetName;
    this.propertyName = propertyName;
    this.relations = relations;
    mapper = new ObjectMapper();

  }

  @Override
  public String getName() {
    return facetName;
  }

  @Override
  public Facet getFacet(GraphTraversal<Vertex, Vertex> searchResult) {
    Map<String, Set<Vertex>> grouped = new HashMap<>();
    List<Facet.Option> options = new ArrayList<>();

    searchResult.as("source").bothE(relations).otherV().has(propertyName).as("target").dedup("source", "target")
            .select("source", "target").forEachRemaining(map -> {
              Vertex source = (Vertex) map.get("source");
              String targetValue = (String) ((Vertex) map.get("target")).property(propertyName).value();
              if (!grouped.containsKey(targetValue)) {
                grouped.put(targetValue, new HashSet<>());
              }
              grouped.get(targetValue).add(source);
            });


    grouped.entrySet().stream().forEach(group -> {
      List<?> facetKeys;
      try {
        facetKeys = (List<?>) mapper.readValue(group.getKey(), List.class);
      } catch (IOException e) {
        LOG.error("'{}' is not a valid multi valued field", group.getKey());
        facetKeys = Lists.newArrayList();
      }
      facetKeys.stream().forEach(facetKey -> options.add(
              new Facet.DefaultOption((String) facetKey, group.getValue().size())));
    });


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
            return o1 instanceof String && o2 instanceof List &&
                    ((List<?>) o2).stream().anyMatch(val -> ((String) o1).contains("\"" + val + "\""));
          }, values)));
        }
      }
    }
  }

  @Override
  public List<String> getValues(Vertex vertex) {
    return null;
  }
}
