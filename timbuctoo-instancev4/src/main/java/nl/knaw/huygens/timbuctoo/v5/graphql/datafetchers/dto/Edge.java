package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

public class Edge {
  private final BoundSubject node;
  private final String cursor;

  public Edge(BoundSubject node, String cursor) {
    this.node = node;
    this.cursor = cursor;
  }

  public BoundSubject getNode() {
    return node;
  }

  public String getCursor() {
    return cursor;
  }

}
