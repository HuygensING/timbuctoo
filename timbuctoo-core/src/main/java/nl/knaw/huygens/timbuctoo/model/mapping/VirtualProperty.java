package nl.knaw.huygens.timbuctoo.model.mapping;

/**
 * A class to help to determine properties used for searching and indexing.
 */
public class VirtualProperty {
  private final String propertyName;
  private final String accessor;

  public VirtualProperty(String propertyName, String accessor) {
    this.propertyName = propertyName;
    this.accessor = accessor;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getAccessor() {
    return accessor;
  }
}
