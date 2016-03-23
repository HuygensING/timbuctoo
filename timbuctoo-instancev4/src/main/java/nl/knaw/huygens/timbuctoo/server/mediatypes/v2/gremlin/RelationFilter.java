package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;


import nl.knaw.huygens.timbuctoo.search.EntityRef;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.List;

public class RelationFilter implements QueryFilter, Resultable {

  private String name;
  private String type;
  private String targetDomain;
  private Direction direction;
  private List<QueryFilter> filters;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTargetDomain() {
    return targetDomain;
  }

  public void setTargetDomain(String targetDomain) {
    this.targetDomain = targetDomain;
  }

  public String getDirection() {
    return direction.toString().toLowerCase();
  }

  public void setDirection(String direction) {
    this.direction = Direction.valueOf(direction.toUpperCase());
  }

  public List<QueryFilter> getOr() {
    return filters;
  }

  public void setOr(List<QueryFilter> filters) {
    this.filters = filters;
  }


  @Override
  public GraphTraversal getTraversal() {
    GraphTraversal[] traversals = filters.stream()
            .map(QueryStep::getTraversal).toArray(GraphTraversal[]::new);


    switch (direction) {
      case OUT:
        return __.outE(name).otherV().or(traversals);
      case IN:
        return __.inE(name).otherV().or(traversals);
      default:
        return __.bothE(name).otherV().or(traversals);
    }
  }

  @Override
  public QueryStep setDomain(String domain) {
    return this;
  }

  @Override
  public Long getResultCount() {
    return null;
  }

  @Override
  public List<EntityRef> getResults() {
    return null;
  }

  @Override
  public String toString() {
    return "RelationFilter{" +
            "name='" + name + '\'' +
            ", type='" + type + '\'' +
            ", targetDomain='" + targetDomain + '\'' +
            ", direction=" + direction +
            ", filters=" + filters +
            '}';
  }
}
