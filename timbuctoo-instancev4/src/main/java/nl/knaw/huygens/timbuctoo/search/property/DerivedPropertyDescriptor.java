package nl.knaw.huygens.timbuctoo.search.property;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.PropertyDescriptor;
import nl.knaw.huygens.timbuctoo.search.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

public class DerivedPropertyDescriptor implements PropertyDescriptor {
  private final String relationName;
  private final String propertyName;
  private final PropertyParser parser;

  public DerivedPropertyDescriptor(String relationName, String propertyName, PropertyParser parser) {
    this.relationName = relationName;
    this.propertyName = propertyName;
    this.parser = parser;
  }

  @Override
  public String get(Vertex vertex) {
    List<String> values = Lists.newArrayList();

    vertex.vertices(Direction.OUT, relationName).forEachRemaining(vertex1 -> {
      values.add(parser.parse(vertex1.value(propertyName)));
    });

    return values.isEmpty() ? null : String.join(";", values);
  }
}
