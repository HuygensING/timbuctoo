package nl.knaw.huygens.repository.util;

import org.restlet.data.Form;

public class ResourceUtils {

  public static boolean getBooleanParam(Form form, String name, boolean defaultValue) {
    String value = form.getFirstValue(name);
    if (value != null) {
      return Boolean.parseBoolean(value);
    }
    return defaultValue;
  }

  public static int getIntParam(Form form, String name, int defaultValue) {
    String value = form.getFirstValue(name);
    if (value != null) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        // swallow
      }
    }
    return defaultValue;
  }

  public static int getIntParam(Form form, String name, int minValue, int maxValue, int defaultValue) {
    if (form == null) {
      return defaultValue;
    }
    String value = form.getFirstValue(name);
    if (value != null) {
      try {
        return toRange(Integer.parseInt(value), minValue, maxValue);
      } catch (NumberFormatException e) {
        // swallow
      }
    }
    return defaultValue;
  }

  public static int toRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }

  private ResourceUtils() {
    throw new AssertionError("Non-instantiable class");
  }

}
