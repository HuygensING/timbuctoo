package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.FacetValue;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import nl.knaw.huygens.timbuctoo.search.description.facet.helpers.ListFacetGetter;
import nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search.ListFacetValue;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.apache.tinkerpop.gremlin.process.traversal.P.within;

public class DerivedListFacetDescription implements FacetDescription {
  private final String facetName;
  private final String propertyName;
  private final PropertyParser parser;
  private final String[] relations;
  private final String[] relationNames;
  private final FacetGetter facetGetter;

  private DerivedListFacetDescription(String facetName, String propertyName, PropertyParser parser,
                                      String[] relationNames, String... relations) {

    this.facetName = facetName;
    this.propertyName = propertyName;
    this.parser = parser;
    this.relations = relations;
    this.relationNames = relationNames;
    this.facetGetter = new ListFacetGetter(parser);
  }

  public DerivedListFacetDescription(String facetName, String propertyName, String relationName,
                                     PropertyParser parser, String... relations) {
    this(facetName, propertyName, parser, new String[]{relationName}, relations);
  }

  public DerivedListFacetDescription(String facetName, String propertyName, List<String> relationNames,
                                     PropertyParser parser, String... relations) {
    this(facetName, propertyName, parser, relationNames.toArray(new String[relationNames.size()]), relations);
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
            .bothE(relationNames).otherV()
            .values(propertyName)
            .map(value -> parser.parse((String) value.get()))
            .is(within(values)));
  }

  @Override
  public String getName() {
    return facetName;
  }

  @Override
  public Facet getFacet(Map<String, Set<Vertex>> values) {
    return facetGetter.getFacet(facetName, values);
  }

  @Override
  public List<String> getValues(Vertex vertex) {
    List<String> result = new ArrayList<>();
    vertex.vertices(Direction.BOTH, relations).forEachRemaining(targetVertex -> {
      targetVertex.vertices(Direction.BOTH, relationNames).forEachRemaining(finalVertex -> {
        if (finalVertex.property(propertyName).isPresent()) {
          result.add((String) finalVertex.property(propertyName).value());
        }
      });
    });
    return result;
  }
}
