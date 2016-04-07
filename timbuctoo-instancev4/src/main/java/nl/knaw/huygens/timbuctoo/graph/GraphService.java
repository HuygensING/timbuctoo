package nl.knaw.huygens.timbuctoo.graph;

import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.UUID;

public class GraphService {
  private final GraphWrapper graphWrapper;

  public GraphService(GraphWrapper wrapper) {
    this.graphWrapper = wrapper;
  }

  public D3Graph get(String type, UUID uuid, List<String> relationNames, int depth) throws NotFoundException {
    GraphTraversal<Vertex, Vertex> result = graphWrapper.getGraph().traversal().V()
            .has("tim_id", uuid.toString()).filter(
              x -> ((String) x.get().property("types").value()).contains("\"" + type + "\"")
            ).has("isLatest", true)
            .not(__.has("deleted", true));

    if (!result.hasNext()) {
      throw new NotFoundException();
    }


    Vertex vertex = result.next();
    D3Graph d3Graph = new D3Graph();

    generateD3Graph(d3Graph, vertex, relationNames, depth, 1);

    return d3Graph;
  }

  private void generateD3Graph(D3Graph d3Graph, Vertex vertex, List<String> relationNames,
                               int depth, int currentDepth) {

    d3Graph.addNode(vertex);
    vertex.edges(Direction.BOTH, relationNames.toArray(new String[relationNames.size()])).forEachRemaining(edge -> {
      loadLinks(d3Graph, relationNames, depth, currentDepth, edge);

    });
  }

  private void loadLinks(D3Graph d3Graph, List<String> relationNames,
                         int depth, int currentDepth, Edge edge) {

    Vertex source = edge.inVertex();
    Vertex target = edge.outVertex();
    d3Graph.addNode(source);
    d3Graph.addNode(target);
    d3Graph.addLink(edge, source, target);

    if (currentDepth < depth) {
      generateD3Graph(d3Graph, source, relationNames, depth, currentDepth + 1);
      generateD3Graph(d3Graph, target, relationNames, depth, currentDepth + 1);
    }
  }
}
