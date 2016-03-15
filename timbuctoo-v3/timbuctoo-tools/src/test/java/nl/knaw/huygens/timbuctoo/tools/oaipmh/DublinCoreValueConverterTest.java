package nl.knaw.huygens.timbuctoo.tools.oaipmh;

/*
 * #%L
 * Timbuctoo tools
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

public class DublinCoreValueConverterTest {
  private DublinCoreValueConverter instance;

  @Before
  public void setUp() {
    instance = new DublinCoreValueConverter();
  }

  @Test
  public void convertReturnsTheToStringByDefault() {
    // setup
    Integer value = new Integer(5);

    // action
    String convertedValue = instance.convert(value);

    // verify
    assertThat(convertedValue, is(equalTo(value.toString())));
  }

  @Test
  public void convertOfAStringReturnsTheString() {
    // setup
    String value = "StringValue";

    // action
    String convertedValue = instance.convert(value);

    // verify
    assertThat(convertedValue, is(equalTo(value)));
  }

  @Test(expected = NullPointerException.class)
  public void convertThrowsANullPointerExceptionInTheValueIsNull() {
    instance.convert(null);
  }
}
