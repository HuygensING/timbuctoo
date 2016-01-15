package nl.knaw.huygens.timbuctoo.server.rest;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.stream.Collectors;

public class TimbuctooQuery {
  private final WwPersonSearchDescription description;

  public TimbuctooQuery(WwPersonSearchDescription description) {
    this.description = description;
  }

  public SearchResult execute(Graph graph) {
    List<Vertex> vertices = description.filterByType(graph.traversal().V()).toList();

    List<EntityRef> refs = vertices.stream().map(vertex -> description.createRef(vertex)).collect(Collectors.toList());
    return new SearchResult(refs, description);
  }
}
