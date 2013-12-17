package nl.knaw.huygens.timbuctoo.util;

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

public class TimeUtils {

  public static long hhmmssToMillis(String text) {
    if (text.matches("^\\d\\d:\\d\\d:\\d\\d$")) {
      int hours = Integer.parseInt(text.substring(0, 2));
      int minutes = Integer.parseInt(text.substring(3, 5));
      int seconds = Integer.parseInt(text.substring(6, 8));
      return (((hours * 60L + minutes) * 60L) + seconds) * 1000L;
    }
    return -1;
  }

  private TimeUtils() {
    throw new AssertionError("Non-instantiable class");
  }

}
