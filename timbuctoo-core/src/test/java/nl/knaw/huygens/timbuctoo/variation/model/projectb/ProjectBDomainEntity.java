package nl.knaw.huygens.timbuctoo.variation.model.projectb;

import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;

import com.google.common.base.Objects;

public class ProjectBDomainEntity extends BaseDomainEntity {

  public String projectBGeneralTestDocValue;

  public ProjectBDomainEntity() {}

  public ProjectBDomainEntity(String id) {
    super(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ProjectBDomainEntity)) {
      return false;
    }

    boolean isEqual = super.equals(obj);

    ProjectBDomainEntity other = (ProjectBDomainEntity) obj;

    isEqual &= Objects.equal(other.projectBGeneralTestDocValue, projectBGeneralTestDocValue);

    return isEqual;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ProjectBGeneralTestDoc { \ngeneralTestDocValue: ");
    sb.append(generalTestDocValue);
    sb.append("\nid: ");
    sb.append(getId());
    sb.append("\nroles: ");
    sb.append(getRoles());
    sb.append("\npid: ");
    sb.append(getPid());
    sb.append("\nprojectBGeneralTestDocValue: ");
    sb.append(projectBGeneralTestDocValue);
    sb.append("\n}");

    return sb.toString();
  }

}
