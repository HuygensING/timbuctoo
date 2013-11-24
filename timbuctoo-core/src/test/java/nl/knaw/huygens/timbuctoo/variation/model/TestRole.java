package nl.knaw.huygens.timbuctoo.variation.model;

import nl.knaw.huygens.timbuctoo.model.Role;

import com.google.common.base.Objects;

public class TestRole extends Role {

  private String roleName;

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TestRole)) {
      return false;
    }

    TestRole other = (TestRole) obj;

    return Objects.equal(other.roleName, roleName);
  }

  @Override
  public String toString() {
    return "TestRole{\nroleName: " + roleName + "\n}";
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(roleName);
  }

}
