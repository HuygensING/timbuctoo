package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;


import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin.VertexMapper.mapVertex;

public class CollectionQuery implements QueryFilter, Resultable {

  private static final String TYPE = "entity";
  private String domain;
  private List<QueryFilter> filters;
  private List<EntityRef> results = new ArrayList<>();
  private Set<String> resultIds = new HashSet<>();
  private Vres vres;

  public void setVres(Vres vres) {
    this.vres = vres;
    this.getAnd().forEach(queryFilter -> queryFilter.setVres(vres));
  }

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

  private Consumer<Traverser<Object>> loadResult(GraphTraversalSource traversalSource, String domain) {
    return vertexT -> {
      final GraphTraversal<?, Try<JsonNode>> displayNameT =
        vres.getCollectionForType(domain).get().getDisplayName().traversal().sideEffect(x -> {
          x.get().onSuccess(node -> {
            final String timId = (String) ((Vertex) vertexT.get()).property("tim_id").value();
            EntityRef entityRef = new EntityRef(domain, timId);
            entityRef.setDisplayName(node.asText());
            if (!resultIds.contains(timId)) {
              results.add(entityRef);
            }
            resultIds.add(timId);
          });
        });

      traversalSource.V(((Vertex) vertexT.get()).id()).union(displayNameT).forEachRemaining(x -> {

      });
    };
  }

  @Override
  public GraphTraversal getTraversal(GraphTraversalSource traversalSource) {


    GraphTraversal[] traversals = filters.stream()
            .map(f -> {
              f.setDomain(this.domain);
              return f;
            })
            .map((queryFilter) -> queryFilter.getTraversal(traversalSource))
            .toArray(GraphTraversal[]::new);


    return __.has(T.label, LabelP.of(getDomain()))
                .and(traversals).sideEffect(loadResult(traversalSource, domain));
  }


}
