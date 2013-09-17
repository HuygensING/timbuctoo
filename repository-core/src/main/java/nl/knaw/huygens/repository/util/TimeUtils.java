package nl.knaw.huygens.repository.util;

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
