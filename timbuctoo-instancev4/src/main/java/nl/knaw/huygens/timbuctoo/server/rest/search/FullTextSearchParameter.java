package nl.knaw.huygens.timbuctoo.server.rest.search;

public class FullTextSearchParameter {
  private String name;
  private String value;

  public FullTextSearchParameter(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
