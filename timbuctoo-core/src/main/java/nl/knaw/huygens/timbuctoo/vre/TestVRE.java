package nl.knaw.huygens.timbuctoo.vre;

import java.io.IOException;

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

public class TestVRE implements VRE {
  private Scope scope;

  public TestVRE() throws IOException {
    scope = new TestScope();
  }

  @Override
  public String getName() {
    return "TestVRE";
  }

  @Override
  public Scope getScope() {
    return scope;
  }

}
