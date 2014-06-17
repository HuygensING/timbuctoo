package nl.knaw.huygens.timbuctoo.graph;

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

public class D3Link {

  private final int source;
  private final int target;
  private final String type;

  public D3Link(int source, int target, String type) {
    this.source = source;
    this.target = target;
    this.type = type;
  }

  public int getSource() {
    return source;
  }

  public int getTarget() {
    return target;
  }

  public String getType() {
    return type;
  }

}
