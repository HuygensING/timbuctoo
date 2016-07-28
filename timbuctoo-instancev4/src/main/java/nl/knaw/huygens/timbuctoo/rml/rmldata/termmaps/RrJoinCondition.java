package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

public class RrJoinCondition {
  private final String child;
  private final String parent;

  public RrJoinCondition(String child, String parent) {
    this.child = child;
    this.parent = parent;
  }

  public String getChild() {
    return child;
  }

  public String getParent() {
    return parent;
  }
}
