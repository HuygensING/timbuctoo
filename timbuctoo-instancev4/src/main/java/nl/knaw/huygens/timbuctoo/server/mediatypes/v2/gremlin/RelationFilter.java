package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;


import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin.VertexMapper.mapVertex;

public class RelationFilter implements QueryFilter, Resultable {

  private String name;
  private String type;
  private String targetDomain;
  private Direction direction;
  private List<QueryFilter> filters;
  private List<EntityRef> results = new ArrayList<>();
  private Set<String> resultIds = new HashSet<>();
  private Vres vres;

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


  @Override
  public void setVres(Vres vres) {
    this.vres = vres;
    this.getOr().forEach(queryFilter -> queryFilter.setVres(vres));
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

  private Traverser loadResult(Traverser traverser) {
    Edge edge = (Edge) traverser.get();
    StringBuilder idBuilder = new StringBuilder();
    Vertex outVertex = edge.outVertex();
    Vertex inVertex = edge.inVertex();
    idBuilder.append((String) outVertex.property("tim_id").value())
            .append("_")
            .append((String) edge.inVertex().property("tim_id").value());

    final String id = idBuilder.toString();

    if (!resultIds.contains(id)) {
      EntityRef outResult = mapVertex(outVertex);
      EntityRef inResult = mapVertex(inVertex);
      EntityRef result = new EntityRef(name, id);
      String displayName = outResult.getDisplayName() +
              " " +
              name +
              " " +
              inResult.getDisplayName();

      result.setDisplayName(displayName);
      Map<String, Object> data = new HashMap<>();
      data.put("out", outResult);
      data.put("in", inResult);
      result.setData(data);
      results.add(result);
    }
    resultIds.add(id);
    return traverser;
  }

  @Override
  public GraphTraversal getTraversal(GraphTraversalSource traversalSource) {
    GraphTraversal[] traversals = filters
      .stream().map((queryFilter) -> queryFilter.getTraversal(traversalSource)).toArray(GraphTraversal[]::new);


    switch (direction) {
      case OUT:
        return __.outE(name).where(__.otherV().or(traversals)).map(this::loadResult);
      case IN:
        return __.inE(name).where(__.otherV().or(traversals)).map(this::loadResult);
      default:
        return __.bothE(name).where(__.otherV().or(traversals)).map(this::loadResult);
    }
  }

  @Override
  public QueryFilter setDomain(String domain) {
    return this;
  }

  @Override
  public Long getResultCount() {
    return (long) resultIds.size();
  }

  @Override
  public List<EntityRef> getResults() {
    return results;
  }

}
