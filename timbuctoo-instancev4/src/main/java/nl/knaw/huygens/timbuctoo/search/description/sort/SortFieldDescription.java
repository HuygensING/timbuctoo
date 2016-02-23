package nl.knaw.huygens.timbuctoo.search.description.sort;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class SortFieldDescription {
  private final String name;
  private final GraphTraversal<?, ?> traversal;

  public SortFieldDescription(String name, GraphTraversal<?, ?> traversal) {

    this.name = name;
    this.traversal = traversal;
  }

  public String getName() {
    return name;
  }

  public GraphTraversal<?, ?> getTraversal() {
    return traversal;
  }
}
