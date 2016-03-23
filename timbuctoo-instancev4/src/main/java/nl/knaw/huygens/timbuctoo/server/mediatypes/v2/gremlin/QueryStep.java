package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public interface QueryStep {
  @JsonIgnore
  GraphTraversal getTraversal();

  QueryStep setDomain(String domain);
}
