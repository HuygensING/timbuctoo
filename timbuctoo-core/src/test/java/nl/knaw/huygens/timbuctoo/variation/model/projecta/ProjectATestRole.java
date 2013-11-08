package nl.knaw.huygens.timbuctoo.variation.model.projecta;

import nl.knaw.huygens.timbuctoo.variation.model.TestRole;

import com.google.common.base.Objects;

public class ProjectATestRole extends TestRole {
  private String projectATestRoleName;

  public String getProjectATestRoleName() {
    return projectATestRoleName;
  }

  public void setProjectATestRoleName(String projectATestRoleName) {
    this.projectATestRoleName = projectATestRoleName;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ProjectATestRole)) {
      return false;
    }
    ProjectATestRole other = (ProjectATestRole) obj;
    boolean isEqual = super.equals(obj);
    isEqual &= Objects.equal(other.projectATestRoleName, projectATestRoleName);

    return isEqual;
  }

  @Override
  public String toString() {
    return "ProjectATestRole{\nroleName: " + getRoleName() + "\nprojectATestRoleName: " + projectATestRoleName + "\n}";
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getRoleName(), projectATestRoleName);
  }
}
