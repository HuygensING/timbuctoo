package nl.knaw.huygens.timbuctoo.graph;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

public class Graph {

  private final Map<String, Vertex> vertices;

  public Graph() {
    vertices = Maps.newTreeMap();
  }

  public int numberOfVertices() {
    return vertices.size();
  }

  public Collection<Vertex> getVertices() {
    return vertices.values();
  }

  /**
   * Returns the vertex with the specified name, or create a new
   * one if it does not yet exist.
   * @param name the name of the vertex.
   * @return the vertex.
   */
  private Vertex getVertex(String name) {
    Vertex vertex = vertices.get(name);
    if (vertex == null) {
      vertex = new Vertex(name);
      vertices.put(name, vertex);
    }
    return vertex;
  }

  private Edge getEdge(String srce, String dest) {
    Vertex s = getVertex(srce);
    Vertex d = getVertex(dest);
    return s.getEdgeTo(d);
  }

  public void addWeightToEdge(String srce, String dest, int weight) {
    Edge edge = getEdge(srce, dest);
    edge.addToWeight(weight);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    Map<String, Integer> map = Maps.newHashMap();
    builder.append(String.format("*Nodes%n"));
    builder.append(String.format("id*int label*string%n"));
    int index = 0;
    for (Vertex vertex : getVertices()) {
      index++;
      map.put(vertex.getName(), index);
      builder.append(String.format("%d\t%s%n", index, vertex.getName()));
    }

    builder.append(String.format("*DirectedEdges%n"));
    builder.append(String.format("source*int target*int weight*int%n"));
    for (Vertex vertex : getVertices()) {
      int source = map.get(vertex.getName());
      for (Edge edge : vertex.getEdges()) {
        int target = map.get(edge.getDestVertex().getName());
        builder.append(String.format("%d\t%d\t%d%n", source, target, edge.getWeight()));
      }
    }

    return builder.toString();
  }

  public static void main(String[] args) {
    Graph graph = new Graph();
    graph.addWeightToEdge("a", "b", 1);
    graph.addWeightToEdge("a", "c", 2);
    graph.addWeightToEdge("a", "d", 3);
    graph.addWeightToEdge("c", "b", 2);
    graph.addWeightToEdge("a", "b", 5);
    System.out.println(graph);
  }

}
