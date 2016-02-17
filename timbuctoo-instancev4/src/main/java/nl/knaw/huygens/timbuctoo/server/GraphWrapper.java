package nl.knaw.huygens.timbuctoo.server;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;

public interface GraphWrapper {
  Graph getGraph();

  /**
   * Returns a GraphTraversalSource that is filtered that only contains the latest edges an vertices.
   * @return the filtered graph as GraphTraversalSource
   */
  GraphTraversalSource getLatestState();
}
