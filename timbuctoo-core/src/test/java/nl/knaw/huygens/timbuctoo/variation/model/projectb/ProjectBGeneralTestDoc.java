package nl.knaw.huygens.timbuctoo.variation.model.projectb;

import nl.knaw.huygens.timbuctoo.variation.model.GeneralTestDoc;

import com.google.common.base.Objects;

public class ProjectBGeneralTestDoc extends GeneralTestDoc {

  public String projectBGeneralTestDocValue;

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ProjectBGeneralTestDoc)) {
      return false;
    }

    boolean isEqual = super.equals(obj);

    ProjectBGeneralTestDoc other = (ProjectBGeneralTestDoc) obj;

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
    sb.append("\ncurrentVariation: ");
    sb.append(getCurrentVariation());
    sb.append("\nprojectBGeneralTestDocValue: ");
    sb.append(projectBGeneralTestDocValue);
    sb.append("\n}");

    return sb.toString();
  }

}
