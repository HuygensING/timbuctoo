package nl.knaw.huygens.timbuctoo.model.util;

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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PeriodHelperTest {

  @Test
  public void createPeriodTwoYears() {
    assertEquals("2001 - 3001", PeriodHelper.createPeriod("2001", "3001"));
  }

  @Test
  public void createPeriodBeginNull() {
    assertEquals("3001 - 3001", PeriodHelper.createPeriod(null, "3001"));
  }

  @Test
  public void createPeriodBeginEmpty() {
    assertEquals("3001 - 3001", PeriodHelper.createPeriod("", "3001"));
  }

  @Test
  public void createPeriodBeginWhiteSpace() {
    assertEquals("3001 - 3001", PeriodHelper.createPeriod(" ", "3001"));
  }

  @Test
  public void createPeriodEndNull() {
    assertEquals("2001 - 2001", PeriodHelper.createPeriod("2001", null));
  }

  @Test
  public void createPeriodEndEmpty() {
    assertEquals("2001 - 2001", PeriodHelper.createPeriod("2001", ""));
  }

  @Test
  public void createPeriodEndWhiteSpace() {
    assertEquals("2001 - 2001", PeriodHelper.createPeriod("2001", " "));
  }

  @Test
  public void createPeriodBothNull() {
    assertEquals(null, PeriodHelper.createPeriod(null, null));
  }

  @Test
  public void createPeriodBothEmptyString() {
    assertEquals(null, PeriodHelper.createPeriod("", ""));
  }

  @Test
  public void createPeriodBothWiteSpace() {
    assertEquals(null, PeriodHelper.createPeriod(" ", " "));
  }
}
