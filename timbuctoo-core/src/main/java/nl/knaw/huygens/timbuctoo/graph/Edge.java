package nl.knaw.huygens.timbuctoo.graph;

public class Edge {

  private final Vertex destVertex;
  private int weight;

  public Edge(Vertex destVertex) {
    this.destVertex = destVertex;
    weight = 0;
  }

  public Vertex getDestVertex() {
    return destVertex;
  }

  public int getWeight() {
    return weight;
  }

  public void addToWeight(int value) {
    weight += value;
  }

}
