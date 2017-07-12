package nl.knaw.huygens.timbuctoo.v5.serializable.serializations.base;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2017-06-07 10:18.
 */
public class Entity {

  private final String id;
  private final String uri;
  private List<Edge> inEdges = new ArrayList<>();
  private List<Edge> outEdges = new ArrayList<>();

  public Entity(String uri, int index) {
    this.uri = uri;
    this.id = "n" + index;
  }

  public String getId() {
    return id;
  }

  public Entity(String uri) {
    this(uri, -1);
  }

  public String getUri() {
    return uri;
  }

  public List<Edge> getInEdges() {
    return inEdges;
  }

  public void addInEdge(Edge edge) {
    inEdges.add(edge);
  }

  public List<Edge> getOutEdges() {
    return outEdges;
  }

  public void addOutEdge(Edge edge) {
    outEdges.add(edge);
  }

  @Override
  public String toString() {
    return //super.toString() +
      " { (" + id + ") uri=" + uri + " in=" + inEdges + " out=" + outEdges + "}";
  }
}
