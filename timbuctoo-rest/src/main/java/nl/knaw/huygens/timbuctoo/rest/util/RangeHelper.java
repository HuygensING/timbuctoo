package nl.knaw.huygens.timbuctoo.rest.util;

/*
 * #%L
 * Timbuctoo REST api
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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
