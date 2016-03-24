package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

public abstract class AbstractPropertyValueFilter implements PropertyValueFilter {

  private String label;
  private String domain;
  private String name;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  protected String getPropertyName() {
    StringBuilder propertyName = new StringBuilder();
    if (!name.equals("tim_id")) {
      propertyName.append(domain).append("_");
    }
    propertyName.append(name);
    return propertyName.toString();
  }

  @Override
  public PropertyValueFilter setDomain(String domain) {
    this.domain = domain;
    return this;
  }


  @Override
  public PropertyValueFilter setName(String name) {
    this.name = name;
    return this;
  }
}
