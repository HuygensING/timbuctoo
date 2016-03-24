package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Arrays;
import java.util.List;

public class PropertyEqualsFilter extends AbstractPropertyValueFilter {
  private static final List<String> v2UnquotedProps = Arrays.asList("wwperson_children", "wwcollective_type", "tim_id");
  private static final String TYPE = "value";
  private String value;

  public String getType() {
    return TYPE;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public GraphTraversal getTraversal() {
    final String propertyName = getPropertyName();
    final String matchValue = v2UnquotedProps.contains(propertyName) ?
            getValue() : "\"" + getValue() + "\"";

    return __.has(propertyName).filter(it ->
            ((String) ((Vertex) it.get()).property(propertyName).value()).contains(matchValue));
  }
}
