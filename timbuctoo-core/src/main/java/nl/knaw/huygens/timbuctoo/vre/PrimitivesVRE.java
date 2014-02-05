package nl.knaw.huygens.timbuctoo.vre;

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

import java.io.IOException;

/**
 * Scope containing all primitive domain entities.
 */
public class PrimitivesVRE implements VRE {

  public static final String NAME = "Primitives";

  private final Scope scope;

  public PrimitivesVRE() throws IOException {
    scope = new PrimitivesScope();
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Scope getScope() {
    return scope;
  }

}
