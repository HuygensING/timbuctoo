package nl.knaw.huygens.solr;

/*
 * #%L
 * Timbuctoo search
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

import org.junit.Before;
import org.junit.Test;

public class SearchParametersTest {

  private SearchParameters instance;

  @Before
  public void setUp() {
    instance = new SearchParameters();
  }

  @Test
  public void testSetTermWithNonEmptyString() {
    testSetTerm("test", "test");
  }

  @Test
  public void testSetTermWithEmptyString() {
    testSetTerm("", "*");
  }

  @Test
  public void testSetFilledTermWithEmptyString() {
    instance.setTerm("test");
    testSetTerm("", "*");
  }

  @Test
  public void testSetFilledTermWithNonEmptyString() {
    instance.setTerm("test1");
    testSetTerm("test", "test");
  }

  private void testSetTerm(String newValue, String expectedValue) {
    instance.setTerm(newValue);
    assertEquals(expectedValue, instance.getTerm());
  }

}
