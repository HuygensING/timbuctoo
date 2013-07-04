package nl.knaw.huygens.repository.model;

/**
 * A reference to a document, to be used in other documents.
 * The reference is partially denormalized by including the display name.
 *
 * It should be easy for the UI to retrieve the referred document
 * from the information contained in the reference.
 * However, we do not want to expose the full Java implementation
 * in the database, so it seems proper not to use the full class
 * name but the document type string instead.
 * It is an open question for me wether we should include the variation.
 */
public class DocumentRef {

  /**
   * Utility for creating instances.
   */
  public static <T extends Document> DocumentRef newInstance(Class<T> type, T document) {
    return new DocumentRef(type, document.getId(), document.getDisplayName());
  }

  private Class<? extends Document> type;
  private String id;
  private String displayName;

  // For deserialization...
  public DocumentRef() {}

  public DocumentRef(Class<? extends Document> type, String id, String displayName) {
    this.type = type;
    this.id = id;
    this.displayName = displayName;
  }

  public Class<? extends Document> getType() {
    return type;
  }

  public void setType(Class<? extends Document> type) {
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
