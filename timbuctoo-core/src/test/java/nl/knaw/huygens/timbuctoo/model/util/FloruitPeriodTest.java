package nl.knaw.huygens.timbuctoo.model.util;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

import org.junit.Test;

public class FloruitPeriodTest {

  @Test
  public void testToStringForOneDate() {
    assertThat(new FloruitPeriod("1012"), hasToString("fl. 1012"));
  }

  @Test
  public void testToStringForTwoDatesSingleYear() {
    assertThat(new FloruitPeriod("10121010", "10121212"), hasToString("fl. 1012"));
  }

  @Test
  public void testToStringForMultipleYears() {
    assertThat(new FloruitPeriod("10121010", "10141212"), hasToString("fl. 1012-1014"));
  }

  @Test
  public void testToStringForEarlyYear() {
    assertThat(new FloruitPeriod("0042"), hasToString("fl. 42"));
    assertThat(new FloruitPeriod("042"), hasToString("fl. 42"));
    assertThat(new FloruitPeriod("42"), hasToString("fl. 42"));
  }

}
