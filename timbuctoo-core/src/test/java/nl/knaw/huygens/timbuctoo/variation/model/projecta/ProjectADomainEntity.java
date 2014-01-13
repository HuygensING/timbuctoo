package nl.knaw.huygens.timbuctoo.variation.model.projecta;

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

import nl.knaw.huygens.timbuctoo.variation.model.BaseDomainEntity;

import com.google.common.base.Objects;

public class ProjectADomainEntity extends BaseDomainEntity {

  public String projectAGeneralTestDocValue;

  public ProjectADomainEntity() {}

  public ProjectADomainEntity(String id) {
    setId(id);
  }

  public ProjectADomainEntity(String id, String name) {
    super(id, name);
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
