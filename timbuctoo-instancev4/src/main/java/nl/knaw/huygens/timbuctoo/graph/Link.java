package nl.knaw.huygens.timbuctoo.graph;


import org.apache.tinkerpop.gremlin.structure.Edge;

public class Link {
  private int source;
  private int target;
  private String type;

  public Link(Edge edge, int source, int target) {
    this.source = source;
    this.target = target;
    this.type = edge.label();
  }

  public int getSource() {
    return source;
  }

  public int getTarget() {
    return target;
  }

  public String getType() {
    return type;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }

    if (other == this) {
      return true;
    }

    if (!(other instanceof Link)) {
      return false;
    }

    Link otherLink = (Link) other;

    return otherLink.getType().equals(type) && (
        (otherLink.getSource() == source && otherLink.getTarget() == target) ||
        (otherLink.getSource() == target && otherLink.getTarget() == source));
  }

  @Override
  public String toString() {
    return "Link{" +
            "source=" + source +
            ", target=" + target +
            ", type='" + type + '\'' +
            '}';
  }
}
