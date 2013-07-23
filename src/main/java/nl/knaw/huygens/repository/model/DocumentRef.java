package nl.knaw.huygens.repository.model;

/**
 * A reference to a document, to be used in other documents.
 * The reference is partially denormalized by including the display name.
 *
 * It is an open question whether we should include the variation.
 */
public class DocumentRef {

  /**
   * Utility for creating instances.
   */
  public static <T extends Document> DocumentRef newInstance(String itype, String xtype, T document) {
    return new DocumentRef(itype, xtype, document.getId(), document.getDisplayName());
  }

  private String itype;
  private String xtype;
  private String id;
  private String displayName;

  // For deserialization...
  public DocumentRef() {}

  public DocumentRef(String itype, String xtype, String id, String displayName) {
    this.itype = itype;
    this.xtype = xtype;
    this.id = id;
    this.displayName = displayName;
  }

  public String getIType() {
    return itype;
  }

  public void setIType(String itype) {
    this.itype = itype;
  }

  public String getXType() {
    return xtype;
  }

  public void setXType(String xtype) {
    this.xtype = xtype;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

}
