package nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine;

public class ImportProperty {
  private final ImportPropertyDescription desc;
  private final String value;

  public ImportProperty(ImportPropertyDescription desc, String value) {
    this.desc = desc;
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public Integer getId() {
    return desc.getId();
  }

  public Integer getOrder() {
    return desc.getOrder();
  }

  public String getName() {
    return desc.getPropertyName();
  }
}
