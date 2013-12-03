package test.model.projecta;

import test.model.TestRole;

public class TestRole1 extends TestRole {

  private String property1;

  public TestRole1() {}

  public TestRole1(String property, String property1) {
    super(property);
    setProperty1(property1);
  }

  public String getProperty1() {
    return property1;
  }

  public void setProperty1(String property1) {
    this.property1 = property1;
  }

}
