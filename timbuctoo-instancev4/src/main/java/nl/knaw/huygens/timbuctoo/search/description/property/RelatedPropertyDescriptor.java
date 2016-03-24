package nl.knaw.huygens.timbuctoo.search.description.property;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.description.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Collections;
import java.util.List;

public class RelatedPropertyDescriptor implements PropertyDescriptor {
  private static final String DEFAULT_SEPARATOR = ";";
  private final String relationName;
  private final String propertyName;
  private final PropertyParser parser;
  private final String separator;

  public RelatedPropertyDescriptor(String relationName, String propertyName, PropertyParser parser) {
    this(relationName, propertyName, parser, DEFAULT_SEPARATOR);
  }

  public RelatedPropertyDescriptor(String relationName, String propertyName, PropertyParser parser, String separator) {
    this.relationName = relationName;
    this.propertyName = propertyName;
    this.parser = parser;
    this.separator = separator;
  }


  @Override
  public String get(Vertex vertex) {
    List<String> values = Lists.newArrayList();

    vertex.vertices(Direction.OUT, relationName).forEachRemaining(vertex1 -> {
      if (vertex1.keys().contains(propertyName)) {
        String value = parser.parse(vertex1.value(propertyName));
        if (value != null) {
          values.add(value);
        }
      }
    });

    Collections.sort(values); // The values are sorted in the solrized version of Timbuctoo too.
    return values.isEmpty() ? null : String.join(separator, values);
  }
}
