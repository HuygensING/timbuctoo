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

    return isEqual;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(generalTestDocValue, getId(), getPid());
  }

  @Override
  public String toString() {

    return "GeneralTestDoc { \ngeneralTestDocValue: " + generalTestDocValue + "\nid: " + getId() + "\npid: " + getPid() + "\n}";
  }

}
