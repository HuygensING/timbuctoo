package nl.knaw.huygens.repository.tools.util;

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
