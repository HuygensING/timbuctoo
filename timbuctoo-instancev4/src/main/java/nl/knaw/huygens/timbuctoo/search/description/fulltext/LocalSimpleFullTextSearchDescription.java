package nl.knaw.huygens.timbuctoo.search.description.fulltext;

import nl.knaw.huygens.timbuctoo.search.description.FullTextSearchDescription;
import nl.knaw.huygens.timbuctoo.server.rest.search.FullTextSearchParameter;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Arrays;

public class LocalSimpleFullTextSearchDescription implements FullTextSearchDescription {
  private final String name;
  private final String propertyName;

  public LocalSimpleFullTextSearchDescription(String name, String propertyName) {
    this.name = name;
    this.propertyName = propertyName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void filter(GraphTraversal<Vertex, Vertex> traversal, FullTextSearchParameter fullTextSearchParameter) {
    traversal.where(__.has(propertyName,
      P.test((o1, o2) -> {
        if (!(o1 instanceof String)) {
          return false;
        }

        String propertyValue = ((String) o1).toLowerCase();
        String[] matches = Arrays.stream(StringUtils.split((String) o2))
                                 .map(String::toLowerCase)
                                 .toArray(String[]::new);

        return StringUtils.containsAny(propertyValue, matches);
      }, fullTextSearchParameter.getTerm())));
  }
}
