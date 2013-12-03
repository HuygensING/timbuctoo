package test.model;

import nl.knaw.huygens.timbuctoo.model.Role;

public class TestRole extends Role {

  private String property;

  public TestRole() {}

  public TestRole(String property) {
    setProperty(property);
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

}
