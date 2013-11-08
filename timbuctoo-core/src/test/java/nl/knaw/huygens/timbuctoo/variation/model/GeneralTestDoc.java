package nl.knaw.huygens.timbuctoo.variation.model;

import com.google.common.base.Objects;

public class GeneralTestDoc extends TestConcreteDoc {

  public String generalTestDocValue;

  public GeneralTestDoc() {}

  public GeneralTestDoc(String id) {
    setId(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GeneralTestDoc)) {
      return false;
    }

    GeneralTestDoc other = (GeneralTestDoc) obj;

    boolean isEqual = true;

    isEqual &= Objects.equal(other.generalTestDocValue, generalTestDocValue);
    isEqual &= Objects.equal(other.getId(), getId());
    isEqual &= Objects.equal(other.getPid(), getPid());
    isEqual &= Objects.equal(other.getRoles(), getRoles());

    return isEqual;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(generalTestDocValue, getId(), getPid());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("GeneralTestDoc { \ngeneralTestDocValue: ");
    sb.append(generalTestDocValue);
    sb.append("\nid: ");
    sb.append(getId());
    sb.append("\nroles: ");
    sb.append(getRoles());
    sb.append("\npid: ");
    sb.append(getPid());
    sb.append("\n}");

    return sb.toString();
  }
}
