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

import nl.knaw.huygens.timbuctoo.model.Role;

import com.google.common.base.Objects;

public class NewTestRole extends Role {

  private String newTestRoleName;

  public String getNewTestRoleName() {
    return newTestRoleName;
  }

  public void setNewTestRoleName(String newTestRoleName) {
    this.newTestRoleName = newTestRoleName;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NewTestRole)) {
      return false;
    }

    NewTestRole other = (NewTestRole) obj;

    return Objects.equal(other.newTestRoleName, newTestRoleName);
  }

  @Override
  public String toString() {
    return "NewTestRole{\nnewTestRoleName: " + newTestRoleName + "\n}";
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(newTestRoleName);
  }
}
