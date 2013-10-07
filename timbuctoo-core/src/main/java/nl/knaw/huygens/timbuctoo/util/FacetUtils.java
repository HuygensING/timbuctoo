package nl.knaw.huygens.timbuctoo.util;

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
