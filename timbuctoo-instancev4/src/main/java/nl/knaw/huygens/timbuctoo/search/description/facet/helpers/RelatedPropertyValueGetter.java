package nl.knaw.huygens.timbuctoo.search.description.facet.helpers;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.List;

public class RelatedPropertyValueGetter {

  public static List<String> getValues(Vertex vertex, String propertyName, String... relations) {
    List<String> result = new ArrayList<>();
    vertex.vertices(Direction.BOTH, relations).forEachRemaining(targetVertex -> {
      if (targetVertex.property(propertyName).isPresent()) {
        result.add((String) targetVertex.property(propertyName).value());
      }
    });
    return result;
  }
}
