package test.model.projecta;

import test.model.TestRole;

public class TestRoleA1 extends TestRole {

  private String propertyA1;

  public TestRoleA1() {}

  public TestRoleA1(String property, String propertyA1) {
    super(property);
    setPropertyA1(propertyA1);
  }

  public String getPropertyA1() {
    return propertyA1;
  }

  public void setPropertyA1(String property) {
    propertyA1 = property;
  }

}
