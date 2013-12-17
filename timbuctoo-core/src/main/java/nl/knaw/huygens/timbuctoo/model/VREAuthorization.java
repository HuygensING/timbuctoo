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

import com.google.common.base.Objects;

@IDPrefix("VREA")
public class VREAuthorization extends SystemEntity {
  private String userId;
  private String vreId;
  private List<String> roles;

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getVreId() {
    return vreId;
  }

  public void setVreId(String vreId) {
    this.vreId = vreId;
  }

  @Override
  public String getDisplayName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof VREAuthorization)) {
      return false;
    }

    VREAuthorization other = (VREAuthorization) obj;

    return Objects.equal(other.vreId, vreId) && Objects.equal(other.userId, userId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(vreId, userId);
  }
}
