package nl.knaw.huygens.repository.model.util;

import org.apache.commons.lang.StringUtils;

/**
 * A helper class, that helps to build a period.
 * @author martijnm
 *
 */
public class PeriodHelper {

  /**
   * 
   * @param beginDate should be a year
   * @param endDate should be a year
   * @return
   */
  public static String createPeriod(String beginDate, String endDate) {
    beginDate = StringUtils.isBlank(beginDate) ? endDate : beginDate;
    endDate = StringUtils.isBlank(endDate) ? beginDate : endDate;

    if (StringUtils.isNotBlank(beginDate) && StringUtils.isNotBlank(endDate)) {
      return String.format("%s - %s", beginDate, endDate);
    }

    return null;
  }
}
