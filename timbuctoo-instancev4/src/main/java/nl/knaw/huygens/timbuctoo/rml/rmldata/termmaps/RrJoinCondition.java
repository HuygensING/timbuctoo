package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

public class RrJoinCondition {
  private String child;
  private String parent;

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

  public void flip() {
    String temp = parent;
    parent = child;
    child = temp;
  }
}
