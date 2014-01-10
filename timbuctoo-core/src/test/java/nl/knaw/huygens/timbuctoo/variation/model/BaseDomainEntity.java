package nl.knaw.huygens.timbuctoo.variation.model;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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

public class BaseDomainEntity extends DomainEntity {

  public String name;
  public String generalTestDocValue;

  public BaseDomainEntity() {}

  public BaseDomainEntity(String id) {
    setId(id);
  }

  public BaseDomainEntity(String id, String name) {
    setId(id);
    this.name = name;
  }

  @Override
  @JsonIgnore
  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof BaseDomainEntity)) {
      return false;
    }

    BaseDomainEntity other = (BaseDomainEntity) obj;

    boolean isEqual = true;

    isEqual &= Objects.equal(other.generalTestDocValue, generalTestDocValue);
    isEqual &= Objects.equal(other.getId(), getId());
    isEqual &= Objects.equal(other.getPid(), getPid());
    //Order does not matter for us, so compare with sets.
    isEqual &= Objects.equal(createRoleSet(other.getRoles()), createRoleSet(getRoles()));

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
    sb.append("\n}");

    return sb.toString();
  }

}
