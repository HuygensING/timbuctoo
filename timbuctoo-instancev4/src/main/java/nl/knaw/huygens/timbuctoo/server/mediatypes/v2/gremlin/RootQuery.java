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

public class RootQuery implements QueryStep, Resultable {
  private List<CollectionQuery> filters;

  public List<CollectionQuery> getOr() {
    return filters;
  }

  public void setOr(List<CollectionQuery> filters) {
    this.filters = filters;
  }

  private List<EntityRef> results = new ArrayList<>();
  private Set<String> resultIds = new HashSet<>();

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
            .map(CollectionQuery::getTraversal).toArray(GraphTraversal[]::new);

    return __.or(traversals)
        .map(this::loadResult);
  }

  @Override
  public QueryStep setDomain(String domain) {
    return this;
  }

}
