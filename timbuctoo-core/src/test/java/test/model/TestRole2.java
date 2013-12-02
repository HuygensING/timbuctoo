package test.model;

import nl.knaw.huygens.timbuctoo.model.Role;

public class TestRole2 extends Role {

  private String property;

  public TestRole2() {}

  public TestRole2(String property) {
    setProperty(property);
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

}
