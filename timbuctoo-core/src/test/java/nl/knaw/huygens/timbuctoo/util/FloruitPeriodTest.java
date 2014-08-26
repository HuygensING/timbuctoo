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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import nl.knaw.huygens.timbuctoo.model.util.FloruitPeriod;

import org.junit.Test;

public class FloruitPeriodTest {
  @Test
  public void testToStringForOneDate() {
    String expected = "fl. 1012";

    FloruitPeriod period = new FloruitPeriod("1012");

    assertThat(period.toString(), is(equalTo(expected)));
  }

  @Test
  public void testToStringForTwoDatesSingleYear() {
    String expected = "fl. 1012";

    FloruitPeriod period = new FloruitPeriod("10121010", "10121212");

    assertThat(period.toString(), is(equalTo(expected)));
  }

  @Test
  public void testToStringForMultipleYears() {
    String expected = "fl. 1012 - 1014";

    FloruitPeriod period = new FloruitPeriod("10121010", "10141212");

    assertThat(period.toString(), is(equalTo(expected)));
  }
}
