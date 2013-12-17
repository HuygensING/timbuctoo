package nl.knaw.huygens.timbuctoo.tools.util;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UTCUtils {

  // Implementation note: Date formats are not synchronized.

  public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

  public static String dateToString(Date date) {
    SimpleDateFormat format = new SimpleDateFormat(ISO_FORMAT);
    format.setTimeZone(UTC);
    return format.format(date);
  }

  public static String now() {
    return dateToString(new Date());
  }

  public static Date stringToDate(String text) {
    try {
      SimpleDateFormat format = new SimpleDateFormat(ISO_FORMAT);
      format.setTimeZone(UTC);
      return format.parse(text);
    } catch (ParseException e) {
      return null;
    }
  }

  private UTCUtils() {
    throw new AssertionError("Non-instantiable class");
  }

}
