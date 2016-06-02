package nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine;

public class ImportPropertyDescription {
  private String propertyName;
  private boolean unique;
  private String[] metadata = new String[0];
  private final Integer id;
  private final int order;
  private String type = "basic";

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

  public void setMetadata(String... metadata) {
    this.metadata = metadata;
  }

  public String[] getMetadata() {
    return metadata;
  }

  public Integer getId() {
    return id;
  }

  public int getOrder() {
    return order;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
