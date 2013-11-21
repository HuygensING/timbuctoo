package nl.knaw.huygens.timbuctoo.variation.model.projecta;

import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;

import com.google.common.base.Objects;

public class ProjectADomainEntity extends BaseDomainEntity {

  public String projectAGeneralTestDocValue;

  public ProjectADomainEntity() {}

  public ProjectADomainEntity(String id) {
    setId(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ProjectADomainEntity)) {
      return false;
    }
    ProjectADomainEntity other = (ProjectADomainEntity) obj;

    return super.equals(obj) && Objects.equal(other.projectAGeneralTestDocValue, projectAGeneralTestDocValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ProjectAGeneralTestDoc { \ngeneralTestDocValue: ");
    sb.append(generalTestDocValue);
    sb.append("\nid: ");
    sb.append(getId());
    sb.append("\nroles: ");
    sb.append(getRoles());
    sb.append("\npid: ");
    sb.append(getPid());
    sb.append("\nprojectAGeneralTestDocValue: ");
    sb.append(projectAGeneralTestDocValue);
    sb.append("\n}");

    return sb.toString();
  }

}
