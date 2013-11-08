package nl.knaw.huygens.timbuctoo.variation.model.projecta;

import nl.knaw.huygens.timbuctoo.variation.model.NewTestRole;

import com.google.common.base.Objects;

public class ProjectANewTestRole extends NewTestRole {
  private String projectANewTestRoleName;

  public String getProjectANewTestRoleName() {
    return projectANewTestRoleName;
  }

  public void setProjectANewTestRoleName(String projectANewTestRoleName) {
    this.projectANewTestRoleName = projectANewTestRoleName;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getNewTestRoleName(), projectANewTestRoleName);
  }
}
