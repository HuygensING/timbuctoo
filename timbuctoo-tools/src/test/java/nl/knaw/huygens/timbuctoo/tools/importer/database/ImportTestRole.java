package nl.knaw.huygens.timbuctoo.tools.importer.database;

import nl.knaw.huygens.timbuctoo.model.Role;

import com.google.common.base.Objects;

public class ImportTestRole extends Role {
  private String roleTest;

  public String getRoleTest() {
    return roleTest;
  }

  public void setRoleTest(String roleTest) {
    this.roleTest = roleTest;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ImportTestRole)) {
      return false;
    }

    ImportTestRole other = (ImportTestRole) obj;

    return Objects.equal(other.roleTest, roleTest);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(roleTest);
  }

  @Override
  public String toString() {
    return "ImportTestRole{\nroleTest: " + roleTest + "\n}";
  }
}
