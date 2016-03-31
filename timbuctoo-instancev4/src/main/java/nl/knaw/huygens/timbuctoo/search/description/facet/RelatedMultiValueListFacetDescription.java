package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.facet.helpers.ListFacetGetter;
import nl.knaw.huygens.timbuctoo.search.description.facet.helpers.MultiValuePropertyGetter;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.ListFacetValue;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
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

import static java.util.stream.Collectors.toList;

/**
 * A facet description that creates a "LIST" facet with multi-valued properties from connected vertices.
 */
public class RelatedMultiValueListFacetDescription implements FacetDescription {

  private final String facetName;
  private final String propertyName;
  private final String[] relations;
  private final ListFacetGetter listFacetGetter;

  public RelatedMultiValueListFacetDescription(String facetName, String propertyName, String... relations) {
    this.facetName = facetName;
    this.propertyName = propertyName;
    this.relations = relations;
    this.listFacetGetter = new ListFacetGetter();
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
  public String getName() {
    return facetName;
  }

  @Override
  public Facet getFacet(Map<String, Set<Vertex>> values) {
    return listFacetGetter.getFacet(facetName, values);
  }

  @Override
  public List<String> getValues(Vertex vertex) {
    List<String> result = new ArrayList<>();
    vertex.vertices(Direction.BOTH, relations).forEachRemaining(targetVertex -> {
      List<String> values = MultiValuePropertyGetter.getValues(targetVertex, propertyName);
      values.forEach(result::add);
    });
    return result;
  }
}
