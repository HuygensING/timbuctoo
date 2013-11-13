package nl.knaw.huygens.timbuctoo.variation.model;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Role;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

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
    //Order does not matter for us, so compare with sets.
    isEqual &= Objects.equal(createRoleSet(other.getRoles()), createRoleSet(getRoles()));
    isEqual &= Objects.equal(other.getCurrentVariation(), getCurrentVariation());

    return isEqual;
  }

  private Set<Role> createRoleSet(List<Role> roles) {
    if (roles == null) {
      return null;
    }
    return Sets.newHashSet(roles);
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
    sb.append("\ncurrentVariation: ");
    sb.append(getCurrentVariation());
    sb.append("\n}");

    return sb.toString();
  }
}
