package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;


import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.List;

public abstract class NumberPropertyValueFilter extends AbstractPropertyValueFilter {
  private List<String> values;

  public List<String> getValues() {
    return this.values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }


  protected GraphTraversal prepareTraversal() {

    return __.values(getPropertyName())
            .map(value -> {
              try {
                return Integer.parseInt(((String) value.get()).replace("\"", ""));
              } catch (NumberFormatException e) {
                return null;
              }
            }).filter(it -> it.get() != null);
  }
}
