package nl.knaw.huygens.timbuctoo.search.description.facet;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.search.description.FacetDescription;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class DerivedListFacetDescription implements FacetDescription {
  private final String facetName;
  private final String propertyName;
  private final PropertyParser parser;
  private final String relation;

  public DerivedListFacetDescription(String facetName, String propertyName, PropertyParser parser, String relation) {
    this.facetName = facetName;
    this.propertyName = propertyName;
    this.parser = parser;
    this.relation = relation;
  }

  @Override
  public Facet getFacet(List<Vertex> vertices) {
    Map<String, Long> counts = Maps.newHashMap();

    for (Vertex vertex : vertices) {
      for (Iterator<Vertex> related = vertex.vertices(Direction.OUT, relation); related.hasNext(); ) {
        Vertex next = related.next();
        if (next.keys().contains(propertyName)) {
          String value = parser.parse(next.value(propertyName));
          long count = 1;
          if (counts.containsKey(value)) {
            count = counts.get(value) + 1;
          }
          counts.put(value, count);
        }
      }
    }

    List<Facet.Option> options = counts.entrySet().stream().map(
      count -> new Facet.Option(count.getKey(), count.getValue())).collect(toList());


    return new Facet(facetName, options);
  }
}
