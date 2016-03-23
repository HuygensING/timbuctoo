package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.List;

public class PropertyFilter implements QueryFilter {
  private static final String TYPE = "property";
  private List<PropertyValueFilter> filters;
  private String name;
  private String domain = "";

  public List<PropertyValueFilter> getOr() {
    return filters;
  }

  public void setOr(List<PropertyValueFilter> filters) {
    this.filters = filters;
  }

  public String getType() {
    return TYPE;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public GraphTraversal getTraversal() {
    if (filters.size() == 0) {
      return __.V();
    }

    GraphTraversal[] traversals =
            name.equals("tim_id") ?
            filters.stream().map(filter -> filter.getTraversal(name)).toArray(GraphTraversal[]::new)
                    :
            filters.stream().map(filter -> filter.getTraversal(domain + name)).toArray(GraphTraversal[]::new);

    return __.or(traversals);
  }

  @Override
  public void setDomain(String domain) {
    this.domain = domain + "_";
  }

  @Override
  public String toString() {
    return "PropertyFilter{" +
            "filters=" + filters +
            ", name='" + name + '\'' +
            ", type='" + getType() + '\'' +
            '}';
  }
}
