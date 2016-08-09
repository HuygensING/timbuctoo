package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import com.fasterxml.jackson.databind.JsonNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.search.EntityRef;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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

  private Vres vres = null;

  public void setVres(Vres vres) {
    this.vres = vres;
    this.getOr().forEach(collectionQuery -> collectionQuery.setVres(vres));
  }


  @Override
  public Long getResultCount() {
    return (long) resultIds.size();
  }

  @Override
  public List<EntityRef> getResults() {
    return results;
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
    GraphTraversal[] traversals = filters
      .stream().map((collectionQuery) -> collectionQuery.getTraversal(traversalSource)).toArray(GraphTraversal[]::new);
    final String domain = filters.get(0).getDomain();
    return __.or(traversals)
        .sideEffect(loadResult(traversalSource, domain));
  }



  @Override
  public QueryStep setDomain(String domain) {
    return this;
  }

}
