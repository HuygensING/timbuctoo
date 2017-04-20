package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

public class BoundSubject {

  private final String value;
  private final String type;

  public BoundSubject(String value) {
    this.value = value;
    type = null;
  }

  public BoundSubject(String value, String type) {
    this.value = value;
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return "BoundSubject{" +
      "value='" + value + '\'' +
      ", type='" + type + '\'' +
      '}';
  }
}
