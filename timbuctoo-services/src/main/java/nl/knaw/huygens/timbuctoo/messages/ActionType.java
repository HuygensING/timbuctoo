package nl.knaw.huygens.timbuctoo.messages;

/*
 * #%L
 * Timbuctoo services
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

/**
 * Changed the String representation to an enum.
 * It's designed after http://stackoverflow.com/a/2965252
 * 
 * @author martijnm
 */
public enum ActionType {
  ADD("add"), MOD("mod"), DEL("del"), END("end");

  private final String stringRepresentation;

  ActionType(String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }

  public String getStringRepresentation() {
    return stringRepresentation;
  }

  public static ActionType getFromString(String stringRepresentation) {
    if (stringRepresentation != null) {
      for (ActionType actionType : values()) {
        if (actionType.getStringRepresentation().equals(stringRepresentation)) {
          return actionType;
        }
      }
    }
    return null;
  }

}
