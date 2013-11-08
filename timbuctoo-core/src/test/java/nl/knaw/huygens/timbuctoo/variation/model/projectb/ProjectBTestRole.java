package nl.knaw.huygens.timbuctoo.variation.model.projectb;

import nl.knaw.huygens.timbuctoo.variation.model.TestRole;

import com.google.common.base.Objects;

public class ProjectBTestRole extends TestRole {
  private String beeName;

  public String getBeeName() {
    return beeName;
  }

  public void setBeeName(String beeName) {
    this.beeName = beeName;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ProjectBTestRole)) {
      return false;
    }
    boolean isEqual = super.equals(obj);
    ProjectBTestRole other = (ProjectBTestRole) obj;
    isEqual &= Objects.equal(other.beeName, beeName);

    return isEqual;
  }

  @Override
  public String toString() {
    return "ProjectBTestRole{\nroleName: " + getRoleName() + "\nbeeName: " + beeName + "\n}";
  }
}
