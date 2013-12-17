package nl.knaw.huygens.timbuctoo.model;

/*
 * #%L
 * Timbuctoo core
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

import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;

@IDPrefix(User.ID_PREFIX)
public class User extends SystemEntity {

  // Unique definition of prefix; also used in UserResource
  public static final String ID_PREFIX = "USER";

  private String persistentId; // a unique id to identify the use.
  private String email;
  private String firstName;
  private String lastName;
  private String displayName;
  private String commonName;
  private String organisation;
  private VREAuthorization vreAuthorization;

  public String getPersistentId() {
    return persistentId;
  }

  public void setPersistentId(String persistentId) {
    this.persistentId = persistentId;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof User)) {
      return false;
    }

    User other = (User) obj;

    return Objects.equal(other.persistentId, persistentId) || Objects.equal(other.getId(), getId());

  }

  @Override
  public int hashCode() {
    //Google Objects
    return Objects.hashCode(persistentId);
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getCommonName() {
    return commonName;
  }

  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public VREAuthorization getVreAuthorization() {
    return vreAuthorization;
  }

  public void setVreAuthorization(VREAuthorization vreAuthorization) {
    this.vreAuthorization = vreAuthorization;
  }

  @JsonIgnore
  public String getVreId() {
    return vreAuthorization != null ? vreAuthorization.getVreId() : null;
  }

  @JsonIgnore
  public List<String> getRoles() {
    return vreAuthorization != null ? vreAuthorization.getRoles() : null;
  }

  public String getOrganisation() {
    return organisation;
  }

  public void setOrganisation(String organisation) {
    this.organisation = organisation;
  }

}
