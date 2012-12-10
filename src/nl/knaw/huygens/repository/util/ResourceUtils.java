package nl.knaw.huygens.repository.util;


public class ResourceUtils {
  public static int toRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }

  private ResourceUtils() {
    throw new AssertionError("Non-instantiable class");
  }

}
