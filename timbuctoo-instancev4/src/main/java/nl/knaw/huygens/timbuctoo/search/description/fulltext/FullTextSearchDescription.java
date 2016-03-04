package nl.knaw.huygens.timbuctoo.search.description.fulltext;

import nl.knaw.huygens.timbuctoo.search.description.Property;
import nl.knaw.huygens.timbuctoo.server.rest.search.FullTextSearchParameter;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Arrays;

import static nl.knaw.huygens.timbuctoo.search.description.Property.localProperty;

public class FullTextSearchDescription {
  private final String name;
  private final GraphTraversal<Object, Object> propertyTraversal;

  private FullTextSearchDescription(String name, Property property) {
    this.name = name;
    propertyTraversal = property.getTraversal();
  }

  public static FullTextSearchDescription createLocalSimpleFullTextSearchDescription(
    String name,
    String propertyName) {

    return new FullTextSearchDescription(name, localProperty().withName(propertyName).build());
  }

  public String getName() {
    return name;
  }

  @SuppressWarnings("unchecked")
  public void filter(GraphTraversal<Vertex, Vertex> traversal, FullTextSearchParameter fullTextSearchParameter) {
    traversal.where(__.where(propertyTraversal.is(P.test((o1, o2) -> {
      if (!(o1 instanceof String)) {
        return false;
      }

      String propertyValue = ((String) o1).toLowerCase();
      String[] matches = Arrays.stream(StringUtils.split((String) o2))
                               .map(String::toLowerCase)
                               .toArray(String[]::new);

      return StringUtils.containsAny(propertyValue, matches);
    }, fullTextSearchParameter.getTerm()))));
  }
}
