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

import nl.knaw.huygens.timbuctoo.model.Role;

import com.google.common.base.Objects;

public class ImportTestRole extends Role {
  private String roleTest;

  public String getRoleTest() {
    return roleTest;
  }

  public void setRoleTest(String roleTest) {
    this.roleTest = roleTest;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ImportTestRole)) {
      return false;
    }

    ImportTestRole other = (ImportTestRole) obj;

    return Objects.equal(other.roleTest, roleTest);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(roleTest);
  }

  @Override
  public String toString() {
    return "ImportTestRole{\nroleTest: " + roleTest + "\n}";
  }
}
