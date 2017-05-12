package nl.knaw.huygens.timbuctoo.rml.rmldata.termmaps;

public class RrJoinCondition {
  private String child;
  private String parent;

  public RrJoinCondition(String childField, String parentField) {
    this.child = childField;
    this.parent = parentField;
  }

  public String getChildField() {
    return child;
  }

  public String getParentField() {
    return parent;
  }
}
