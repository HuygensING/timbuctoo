package nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine;

public class ImportPropertyDescription {
  private String propertyName;
  private boolean unique;
  private String relationName;
  private String targetCollection;
  private final Integer id;
  private final int order;

  ImportPropertyDescription(Integer id, int order) {
    this.id = id;
    this.order = order;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public boolean isUnique() {
    return unique;
  }

  public void setRelationName(String relationName) {
    this.relationName = relationName;
  }

  public String getRelationName() {
    return relationName;
  }

  public void setTargetCollection(String targetCollection) {
    this.targetCollection = targetCollection;
  }

  public String getTargetCollection() {
    return targetCollection;
  }

  public boolean isProperty() {
    return relationName == null && targetCollection == null;
  }

  public Integer getId() {
    return id;
  }

  public int getOrder() {
    return order;
  }
}
