package nl.knaw.huygens.timbuctoo.model.dcar;

/*
 * #%L
 * Timbuctoo model
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

import static nl.knaw.huygens.timbuctoo.model.dcar.PeriodHelper.createPeriod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

public class PeriodHelperTest {

  @Test
  public void createPeriodTwoYears() {
    String expected = "2001 - 3001";
    assertThat(createPeriod("2001", "3001"), is(equalTo(expected)));
  }

  @Test
  public void createPeriodNoBegin() {
    String expected = "3001 - 3001";
    assertThat(createPeriod(null, "3001"), is(equalTo(expected)));
    assertThat(createPeriod("", "3001"), is(equalTo(expected)));
    assertThat(createPeriod(" ", "3001"), is(equalTo(expected)));
  }

  @Test
  public void createPeriodNoEnd() {
    String expected = "2001 - 2001";
    assertThat(createPeriod("2001", null), is(equalTo(expected)));
    assertThat(createPeriod("2001", ""), is(equalTo(expected)));
    assertThat(createPeriod("2001", " "), is(equalTo(expected)));
  }

  @Test
  public void createPeriodNoBeginNoEnd() {
    assertThat(createPeriod(null, null), is(nullValue()));
    assertThat(createPeriod("", ""), is(nullValue()));
    assertThat(createPeriod(" ", " "), is(nullValue()));
  }

}
