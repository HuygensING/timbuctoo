package nl.knaw.huygens.timbuctoo.util;

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

public class Text {

  /**
   * Conditionally appends a text to a string builder.
   */
  public static void appendTo(StringBuilder builder, String text, String separator) {
    if (text != null && text.length() > 0) {
      if (builder.length() > 0) {
        builder.append(separator);
      }
      builder.append(text);
    }
  }

  private Text() {
    throw new AssertionError("Non-instantiable class");
  }

}
