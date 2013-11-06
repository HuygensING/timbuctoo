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

    boolean isEqual = super.equals(obj) && Objects.equal(other.projectAGeneralTestDocValue, projectAGeneralTestDocValue);

    return isEqual;
  }

  @Override
  public String toString() {
    return "ProjectAGeneralTestDoc { \ngeneralTestDocValue: " + generalTestDocValue + "\nid: " + getId() + "\npid: " + getPid() + "\n projectAGeneralTestDocValue: " + projectAGeneralTestDocValue
        + "\n}";
  }
}
