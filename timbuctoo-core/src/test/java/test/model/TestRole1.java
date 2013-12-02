package test.model;

import nl.knaw.huygens.timbuctoo.model.Role;

public class TestRole1 extends Role {

  private String property;

  public TestRole1() {}

  public TestRole1(String property) {
    setProperty(property);
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

}
