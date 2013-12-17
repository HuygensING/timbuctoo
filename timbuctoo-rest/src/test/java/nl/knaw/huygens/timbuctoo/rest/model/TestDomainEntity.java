package nl.knaw.huygens.timbuctoo.rest.model;

/*
 * #%L
 * Timbuctoo REST api
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

import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import com.google.common.base.Objects;

@EntityTypeName("testdomainentities")
public class TestDomainEntity extends DomainEntity {

  public String name;

  public TestDomainEntity() {}

  public TestDomainEntity(String id) {
    setId(id);
  }

  public TestDomainEntity(String id, String name) {
    setId(id);
    this.name = name;
  }

  @IndexAnnotation(fieldName = "desc")
  public String getDisplayName() {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TestDomainEntity)) {
      return false;
    }

    TestDomainEntity other = (TestDomainEntity) obj;

    return Objects.equal(other.name, name) && Objects.equal(other.getId(), getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

}
