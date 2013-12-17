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

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

public class FacetUtils {

  public static long cleverParseString(String val) {
    if (!StringUtils.isEmpty(val) && val.trim().matches("^\\d+$")) {
      return Long.parseLong(val);
    }
    return 0;
  }

  public static String categorizeNumeric(long n, long resolution) {
    return categorizeNumeric(n, resolution, 0, Integer.MAX_VALUE);
  }

  public static String[] categorizeNumeric(long min, long max, long resolution) {
    return categorizeNumeric(min, max, resolution, 0, Integer.MAX_VALUE);
  }

  public static String[] categorizeNumeric(long min, long max, long resolution, long lowerBound, long upperBound) {
    List<String> rv = Lists.newArrayList(categorizeNumeric(min, resolution, lowerBound, upperBound));
    if (max != min) {
      rv.add(categorizeNumeric(max, resolution, lowerBound, upperBound));
    }
    return rv.toArray(new String[rv.size()]);
  }

  public static String categorizeNumeric(long n, long resolution, long lowerBound, long upperBound) {
    String category;
    if (n < lowerBound) {
      category = String.format(" < %d", lowerBound);
    } else if (n > upperBound) {
      category = String.format(" > %d", upperBound);
    } else {
      long catMin = n - (n % resolution);
      category = String.format("%d - %d", catMin, catMin + resolution);
    }
    return category;
  }
}
