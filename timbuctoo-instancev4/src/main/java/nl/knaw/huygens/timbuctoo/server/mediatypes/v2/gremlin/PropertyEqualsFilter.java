package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class PropertyEqualsFilter implements PropertyValueFilter {
  private static final String TYPE = "value";
  private String value;
  private String label;
  private String domain;
  private String name;

  public String getType() {
    return TYPE;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public GraphTraversal getTraversal() {
    StringBuilder propertyName = new StringBuilder();
    if (!name.equals("tim_id")) {
      propertyName.append(domain).append("_");
    }
    propertyName.append(name);
    return __.has(propertyName.toString()).filter(it ->
            ((String) ((Vertex) it.get()).property(propertyName.toString()).value()).contains(value));
  }

  @Override
  public PropertyEqualsFilter setDomain(String domain) {
    this.domain = domain;
    return this;
  }

  @Override
  public String toString() {
    return "PropertyEqualsFilter{" +
            "type='" + TYPE + '\'' +
            ", value=" + value +
            ", label='" + label + '\'' +
            '}';
  }

  @Override
  public PropertyValueFilter setName(String name) {
    this.name = name;
    return this;
  }
}
