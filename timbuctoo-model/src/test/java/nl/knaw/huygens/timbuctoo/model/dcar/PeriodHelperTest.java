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

  private static final String SEPARATOR = " - ";

  @Test
  public void createPeriodTwoYears() {
    assertThat(createPeriod("2001", "3001"), is(equalTo("2001" + SEPARATOR + "3001")));
  }

  @Test
  public void createPeriodBeginNull() {
    assertThat(createPeriod(null, "3001"), is(equalTo("3001" + SEPARATOR + "3001")));
  }

  @Test
  public void createPeriodBeginEmpty() {
    assertThat(createPeriod("", "3001"), is(equalTo("3001" + SEPARATOR + "3001")));
  }

  @Test
  public void createPeriodBeginWhiteSpace() {
    assertThat(createPeriod(" ", "3001"), is(equalTo("3001" + SEPARATOR + "3001")));
  }

  @Test
  public void createPeriodEndNull() {
    assertThat(createPeriod("2001", null), is(equalTo("2001" + SEPARATOR + "2001")));
  }

  @Test
  public void createPeriodEndEmpty() {
    assertThat(createPeriod("2001", ""), is(equalTo("2001" + SEPARATOR + "2001")));
  }

  @Test
  public void createPeriodEndWhiteSpace() {
    assertThat(createPeriod("2001", " "), is(equalTo("2001" + SEPARATOR + "2001")));
  }

  @Test
  public void createPeriodBothNull() {
    assertThat(createPeriod(null, null), is(nullValue()));
  }

  @Test
  public void createPeriodBothEmptyString() {
    assertThat(createPeriod("", ""), is(nullValue()));
  }

  @Test
  public void createPeriodBothWiteSpace() {
    assertThat(createPeriod(" ", " "), is(nullValue()));
  }

}
