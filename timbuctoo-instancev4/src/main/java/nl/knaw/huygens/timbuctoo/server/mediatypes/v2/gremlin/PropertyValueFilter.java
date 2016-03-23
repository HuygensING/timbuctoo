package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.List;

public class PropertyValueFilter {
  private String type;
  private List<String> values;
  private String label;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public void setValue(String value) {
    this.values = new ArrayList<>();
    this.values.add(value);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public GraphTraversal getTraversal(String name) {
    switch (type) {
      case "value": return __.has(name).filter(it ->
              ((String) ((Vertex) it.get()).property(name).value()).contains(values.get(0)) );
      default: return __.V();
    }
  }

  @Override
  public String toString() {
    return "PropertyValueFilter{" +
            "type='" + type + '\'' +
            ", values=" + values +
            ", label='" + label + '\'' +
            '}';
  }
}
