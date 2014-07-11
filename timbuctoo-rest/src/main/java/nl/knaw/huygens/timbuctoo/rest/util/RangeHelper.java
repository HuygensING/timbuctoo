package nl.knaw.huygens.timbuctoo.rest.util;

public class RangeHelper {
  private RangeHelper() {
    throw new AssertionError("Non-instantiable class");
  }

  /**
   * Make sure {@code value} is between {@code minValue} and {@code maxValue}.
   * @param value the value that has to be in the range
   * @param minValue the minimum value of the range
   * @param maxValue the maximum value of the range
   * @return {@code value} if it's in the range, 
   * {@code minValue} if {@code value} is lower than the {@code minValue}, 
   * {@code maxValue} if {@code value} is higher than the {@code maxValue}
   */
  public static int mapToRange(int value, int minValue, int maxValue) {
    return Math.min(Math.max(value, minValue), maxValue);
  }
}
