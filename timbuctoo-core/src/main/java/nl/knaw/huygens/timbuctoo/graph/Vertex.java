package nl.knaw.huygens.timbuctoo.graph;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

public class Vertex {

  private final String name;
  private final Map<Vertex, Edge> adjacencies;

  public Vertex(String name) {
    this.name = name;
    adjacencies = Maps.newHashMap();
  }

  public String getName() {
    return name;
  }

  public Collection<Edge> getEdges() {
    return adjacencies.values();
  }

  /**
   * Returns the edge from this vertex to the specified vertex,
   * creating a new vertex if it does not exist.
   */
  public Edge getEdgeTo(Vertex vertex) {
    Edge edge = adjacencies.get(vertex);
    if (edge == null) {
      edge = new Edge(vertex);
      adjacencies.put(vertex, edge);
    }
    return edge;
  }

}
