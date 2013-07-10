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
  public static <T extends Document> DocumentRef newInstance(String type, T document) {
    return new DocumentRef(type, document.getId(), document.getDisplayName());
  }

  private String type;
  private String id;
  private String displayName;

  // For deserialization...
  public DocumentRef() {}

  public DocumentRef(String type, String id, String displayName) {
    this.type = type;
    this.id = id;
    this.displayName = displayName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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
