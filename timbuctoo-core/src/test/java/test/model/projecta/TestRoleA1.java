package test.model.projecta;

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

import test.model.TestRole;

public class TestRoleA1 extends TestRole {

  private String propertyA1;

  public TestRoleA1() {}

  public TestRoleA1(String property, String propertyA1) {
    super(property);
    setPropertyA1(propertyA1);
  }

  public String getPropertyA1() {
    return propertyA1;
  }

  public void setPropertyA1(String property) {
    propertyA1 = property;
  }

}
