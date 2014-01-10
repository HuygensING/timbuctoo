package nl.knaw.huygens.timbuctoo.util;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
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
