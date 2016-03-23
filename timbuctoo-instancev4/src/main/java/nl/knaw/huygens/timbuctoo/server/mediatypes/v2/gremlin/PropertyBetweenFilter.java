package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.List;

public class PropertyBetweenFilter implements PropertyValueFilter {
  private static final String TYPE = "between";
  private List<String> values;
  private String label;
  private String domain;
  private String name;

  public String getType() {
    return TYPE;
  }

  public List<String> getValues() {
    return this.values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public GraphTraversal getTraversal() {
    return __.V();
  }

  @Override
  public PropertyValueFilter setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public PropertyValueFilter setDomain(String domain) {
    this.domain = domain;
    return this;
  }
}
