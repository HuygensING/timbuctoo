package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.search.description.facet.helpers.ListFacetGetter;
import nl.knaw.huygens.timbuctoo.search.description.facet.helpers.LocalPropertyValueGetter;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.ListFacetValue;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class ListFacetDescription implements FacetDescription {

  private final String facetName;
  private final String propertyName;
  private final PropertyParser parser;
  private final ListFacetGetter listFacetGetter;

  public ListFacetDescription(String facetName, String propertyName, PropertyParser parser) {
    this.facetName = facetName;
    this.propertyName = propertyName;
    this.parser = parser;
    this.listFacetGetter = new ListFacetGetter(parser);
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
    return LocalPropertyValueGetter.getValues(vertex, propertyName);
  }
}
