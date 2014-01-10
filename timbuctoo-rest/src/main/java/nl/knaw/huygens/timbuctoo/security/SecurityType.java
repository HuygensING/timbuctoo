package nl.knaw.huygens.timbuctoo.security;

/*
* #%L
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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public
* License along with this program. If not, see
* <http://www.gnu.org/licenses/gpl-3.0.html>.
* #L%
*/

/**
 * This enum helps to determine which SecurityType is used. 
 */
public enum SecurityType {

  EXAMPLE("example"), DEFAULT("default");

  private String value;

  SecurityType(String value) {
    this.value = value;
  }

  public static SecurityType getFromString(String value) {
    for (SecurityType securityType : SecurityType.values()) {
      if (securityType.value.equals(value)) {
        return securityType;
      }
    }

    throw new IllegalArgumentException("Security type does not exist");
  }

}
