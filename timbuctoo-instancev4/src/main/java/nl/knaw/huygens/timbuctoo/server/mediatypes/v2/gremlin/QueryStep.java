package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

public interface QueryStep {
  @JsonIgnore
  GraphTraversal getTraversal(GraphTraversalSource traversalSource);

  QueryStep setDomain(String domain);
}
