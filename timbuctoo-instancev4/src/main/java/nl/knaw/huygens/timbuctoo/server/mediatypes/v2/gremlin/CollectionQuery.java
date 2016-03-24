package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;


import nl.knaw.huygens.timbuctoo.search.EntityRef;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin.VertexMapper.mapVertex;

public class CollectionQuery implements QueryFilter, Resultable {

  private static final String TYPE = "entity";
  private String domain;
  private List<QueryFilter> filters;
  private List<EntityRef> results = new ArrayList<>();
  private Set<String> resultIds = new HashSet<>();

  public List<QueryFilter> getAnd() {
    return filters;
  }

  public void setAnd(List<QueryFilter> and) {
    this.filters = and;
  }

  public String getDomain() {
    return domain;
  }

  public QueryFilter setDomain(String domain) {
    this.domain = domain;
    return this;
  }

  public String getType() {
    return TYPE;
  }

  @Override
  public Long getResultCount() {
    return (long) resultIds.size();
  }

  @Override
  public List<EntityRef> getResults() {
    return results;
  }

  private Traverser loadResult(Traverser traverser) {
    Vertex result = (Vertex) traverser.get();
    String id = (String) result.property("tim_id").value();
    if (!resultIds.contains(id)) {
      results.add(mapVertex(result));
    }
    resultIds.add(id);
    return traverser;
  }

  @Override
  public GraphTraversal getTraversal() {


    GraphTraversal[] traversals = filters.stream()
            .map(f -> {
              f.setDomain(this.domain);
              return f;
            })
            .map(QueryFilter::getTraversal)
            .toArray(GraphTraversal[]::new);


    return __.where(__.filter(x -> ((String) ((Vertex) x.get())
                    .property("types").value()).contains("\"" + getDomain() + "\"")))
                .and(traversals).map(this::loadResult);
  }
}
