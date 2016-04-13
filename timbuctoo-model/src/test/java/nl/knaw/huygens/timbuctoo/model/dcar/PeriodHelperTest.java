package nl.knaw.huygens.timbuctoo.model.dcar;

/*
 * #%L
 * Timbuctoo model
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.model.dcar.PeriodHelper.createPeriod;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class PeriodHelperTest {

  @Test
  public void createPeriodCreatesADatableWithTheBeginYearEqualToTheFirstArgument() {
    int fromYear = 2001;
    String fromYearString = "" + fromYear;

    assertThat(createPeriod(fromYearString, "3001"), hasProperty("fromYear", equalTo(fromYear)));
  }

  @Test
  public void createPeriodCreatesADatableWithTheEndYearEqualToTheSecondArgument() {
    int toYear = 3001;
    String toYearString = "" + toYear;

    assertThat(createPeriod("2001", toYearString), hasProperty("toYear", equalTo(toYear)));
  }

  @Test
  public void createPeriodWithNoBeginYearReturnsADatableWithABeginYearEqualToTheEndYear() {
    int toYear = 3001;
    String toYearString = "" + toYear;

    assertThat(createPeriod(null, toYearString), hasProperty("fromYear", equalTo(toYear)));
    assertThat(createPeriod("", toYearString), hasProperty("fromYear", equalTo(toYear)));
    assertThat(createPeriod(" ", toYearString), hasProperty("fromYear", equalTo(toYear)));
  }

  @Test
  public void createPeriodWithNoEndADatableWithAEndYearEqualToTheBeginYear() {
    int fromYear = 2001;
    String fromYearString = "" + fromYear;

    assertThat(createPeriod(fromYearString, null), hasProperty("toYear", equalTo(fromYear)));
    assertThat(createPeriod(fromYearString, ""), hasProperty("toYear", equalTo(fromYear)));
    assertThat(createPeriod(fromYearString, " "), hasProperty("toYear", equalTo(fromYear)));
  }

  @Test
  public void createPeriodNoBeginNoEndReturnsNull() {
    assertThat(createPeriod(null, null), is(nullValue()));
    assertThat(createPeriod("", ""), is(nullValue()));
    assertThat(createPeriod(" ", " "), is(nullValue()));
  }

}
