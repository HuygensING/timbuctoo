package nl.knaw.huygens.timbuctoo.util;

import org.junit.Assert;
import org.junit.Test;

public class TimeUtilsTest {

  @Test
  public void testInvalidConversionToMillis() {
    Assert.assertEquals(-1L, TimeUtils.hhmmssToMillis("0:00:00:00")); // length
    Assert.assertEquals(-1L, TimeUtils.hhmmssToMillis("0:00:00")); // length
    Assert.assertEquals(-1L, TimeUtils.hhmmssToMillis("00-00-00")); // separator
    Assert.assertEquals(-1L, TimeUtils.hhmmssToMillis("00:00:ss")); // digits
  }

  @Test
  public void testValidConversionToMillis() {
    Assert.assertEquals(0L, TimeUtils.hhmmssToMillis("00:00:00"));
    Assert.assertEquals(5 * 1000L, TimeUtils.hhmmssToMillis("00:00:05"));
    Assert.assertEquals(5 * 60 * 1000L, TimeUtils.hhmmssToMillis("00:05:00"));
    Assert.assertEquals(5 * 60 * 60 * 1000L, TimeUtils.hhmmssToMillis("05:00:00"));
    Assert.assertEquals(3661000L, TimeUtils.hhmmssToMillis("01:01:01"));
  }

}
