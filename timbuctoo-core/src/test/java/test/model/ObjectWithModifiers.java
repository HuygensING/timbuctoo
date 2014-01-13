package test.model;

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

public class ObjectWithModifiers {

  private String plainString;
  private static String staticString;
  private transient String transientString;
  private volatile String volatileString;

  public String getPlainString() {
    return plainString;
  }

  public void setPlainString(String plainString) {
    this.plainString = plainString;
  }

  public static String getStaticString() {
    return staticString;
  }

  public static void setStaticString(String staticString) {
    ObjectWithModifiers.staticString = staticString;
  }

  public String getTransientString() {
    return transientString;
  }

  public void setTransientString(String transientString) {
    this.transientString = transientString;
  }

  public String getVolatileString() {
    return volatileString;
  }

  public void setVolatileString(String volatileString) {
    this.volatileString = volatileString;
  }

}
