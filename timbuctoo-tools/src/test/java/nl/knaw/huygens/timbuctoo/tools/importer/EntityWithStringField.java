package nl.knaw.huygens.timbuctoo.tools.importer;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;

public class EntityWithStringField extends DomainEntity {

  private String test;

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

  public void setTest(String test) {
    this.test = test;
  }

  public String getTest() {
    return this.test;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof EntityWithStringField)) {
      return false;
    }

    EntityWithStringField other = (EntityWithStringField) obj;

    boolean isEqual = true;
    isEqual &= Objects.equal(other.test, test);
    isEqual &= Objects.equal(getRolesSet(other.getRoles()), getRolesSet(getRoles()));

    return isEqual;
  }

  private Set<Role> getRolesSet(List<Role> roles) {
    if (roles == null) {
      return null;
    }
    return Sets.newHashSet(roles);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(test, getRoles());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("EntityWithStringField{\ntest: ");
    sb.append(test);
    sb.append("\nroles: ");
    sb.append(getRoles());
    sb.append("\n}");

    return sb.toString();
  }

}
