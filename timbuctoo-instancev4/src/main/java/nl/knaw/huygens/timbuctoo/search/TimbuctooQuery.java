package nl.knaw.huygens.timbuctoo.search;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.stream.Collectors;

public class TimbuctooQuery {
  private final SearchDescription description;

  public TimbuctooQuery(SearchDescription description) {
    this.description = description;
  }

  public SearchResult execute(Graph graph) {
    List<Vertex> vertices = description
      .filterByType(graph.traversal().V()).has("isLatest", true)
      .toList();

    List<EntityRef> refs = vertices.stream().map(vertex -> description.createRef(vertex)).collect(Collectors.toList());
    return new SearchResult(refs, description);
  }
}
