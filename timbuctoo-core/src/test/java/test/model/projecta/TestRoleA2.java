package test.model.projecta;

import test.model.TestRole;

public class TestRoleA2 extends TestRole {

  private String propertyA2;

  public TestRoleA2() {}

  public TestRoleA2(String property, String propertyA2) {
    super(property);
    setPropertyA2(propertyA2);
  }

  public String getPropertyA2() {
    return propertyA2;
  }

  public void setPropertyA2(String property) {
    propertyA2 = property;
  }

}
