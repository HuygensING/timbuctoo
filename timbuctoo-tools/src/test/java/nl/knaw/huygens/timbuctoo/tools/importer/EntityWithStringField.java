package nl.knaw.huygens.timbuctoo.tools.importer;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
