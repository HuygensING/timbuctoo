package nl.knaw.huygens.timbuctoo.variation.model.projecta;

import nl.knaw.huygens.timbuctoo.variation.model.GeneralTestDoc;

import com.google.common.base.Objects;

public class ProjectAGeneralTestDoc extends GeneralTestDoc {

  public String projectAGeneralTestDocValue;

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ProjectAGeneralTestDoc)) {
      return false;
    }
    ProjectAGeneralTestDoc other = (ProjectAGeneralTestDoc) obj;

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
