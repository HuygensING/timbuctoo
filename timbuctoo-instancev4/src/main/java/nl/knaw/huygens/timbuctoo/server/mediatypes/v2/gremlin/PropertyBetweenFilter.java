package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.List;

public class PropertyBetweenFilter extends AbstractPropertyValueFilter {
  private static final String TYPE = "between";
  private List<String> values;


  public String getType() {
    return TYPE;
  }

  public List<String> getValues() {
    return this.values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  @Override
  public GraphTraversal getTraversal() {
    return __.V();
  }

}
