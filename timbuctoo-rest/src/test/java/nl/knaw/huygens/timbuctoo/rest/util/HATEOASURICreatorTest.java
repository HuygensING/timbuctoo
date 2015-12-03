package nl.knaw.huygens.timbuctoo.rest.util;

/*
 * #%L
 * Timbuctoo REST api
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

import nl.knaw.huygens.timbuctoo.config.Configuration;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.config.Paths.SEARCH_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HATEOASURICreatorTest {

  public static final String PUBLIC_URL = "http://www.example.com";
  public static final String QUERY_ID = "queryId";
  public static final int ROWS = 10;
  public static final int START = 0;
  public static final int NEXT_START = START + ROWS;
  public static final String VERSION = "version";

  @Before
  public void setup() {
    Configuration config = mock(Configuration.class);
    when(config.getSetting(HATEOASURICreator.PUBLIC_URL)).thenReturn(PUBLIC_URL);
    instance = new HATEOASURICreator(config);
  }

  private HATEOASURICreator instance;

  @Test
  public void createHATEOASURICreatesABaseURIFromThePublicURLAndSearchPathAndAddsTheQueryIdAndQueryParametersStartAndRows() {
    // action
    String hateoasuri = instance.createHATEOASURIAsString(START, ROWS, QUERY_ID);

    // verify
    assertThat(hateoasuri, is(String.format("%s/%s/%s?start=%d&rows=%d", PUBLIC_URL, SEARCH_PATH, QUERY_ID, START, ROWS)));
  }

  @Test
  public void createHATEOASURICreatesABaseURIFromThePublicURLTheVersionPathAndSearchPathAndAddsTheQueryIdAndQueryParametersStartAndRows() {
    // action
    String hateoasuri = instance.createHATEOASURIAsString(START, ROWS, QUERY_ID, VERSION);

    // verify
    assertThat(hateoasuri, is(String.format("%s/%s/%s/%s?start=%d&rows=%d", PUBLIC_URL, VERSION, SEARCH_PATH, QUERY_ID, START, ROWS)));
  }

  @Test
  public void createNextResultsAsStringReturnsTheCurrentStartAddedWithNumberOfRowsIsTotalFoundIsMore() {
    // setup
    int numfoud = 10 * ROWS;

    // action
    String nextLink = instance.createNextResultsAsString(START, ROWS, numfoud, QUERY_ID, VERSION);

    //
    assertThat(nextLink, containsString(String.format("start=%d", NEXT_START)));
    assertThat(nextLink, containsString(String.format("rows=%d", ROWS)));
  }

  @Test
  public void createNextResultsAsStringReturnsTheCurrentStartAddedWithNumberOfRowsIsTotalFoundIsEqual() {
    // setup
    int numfoud = 2 * ROWS;

    // action
    String nextLink = instance.createNextResultsAsString(START, ROWS, numfoud, QUERY_ID, VERSION);

    //
    assertThat(nextLink, containsString(String.format("start=%d", NEXT_START)));
    assertThat(nextLink, containsString(String.format("rows=%d", ROWS)));
  }

  @Test
  public void createNextResultsReturnsNullIfStartAddedWithNumberRowsIsMoreThanTotalFound() {
    // setup
    int numfoud = ROWS - 1;

    // action
    String nextLink = instance.createNextResultsAsString(START, ROWS, numfoud, QUERY_ID, VERSION);

    // verify
    assertThat(nextLink, is(nullValue()));
  }

  @Test
  public void createNextResultsReturnsNullIfStartAddedWithNumberRowsIsEqualToTotalFound() {
    // setup
    int numfoud = ROWS;

    // action
    String nextLink = instance.createNextResultsAsString(START, ROWS, numfoud, QUERY_ID, VERSION);

    // verify
    assertThat(nextLink, is(nullValue()));
  }

  @Test
  public void createNextResultsReturnsTheNextStartAndTheNumberOfRows() {
    // setup
    int numfoud = ROWS + 5;

    // action
    String nextLink = instance.createNextResultsAsString(START, ROWS, numfoud, QUERY_ID, VERSION);

    // verify
    assertThat(nextLink, containsString(String.format("rows=%d", ROWS)));
  }

  @Test
  public void createPrevResultsReturnsTheCurrentStartWithTheRowsSubtractedAndTheRows() {
    // setup
    int currentStart = ROWS;

    // action
    String prevLink = instance.createPrevResultsAsString(currentStart, ROWS, QUERY_ID, VERSION);

    // verify
    assertThat(prevLink, containsString(String.format("start=%d", currentStart - ROWS)));
    assertThat(prevLink, containsString(String.format("rows=%d", ROWS)));
  }

  @Test
  public void createPrevResultsReturnsThe0IfTheCurrentStartWithTheRowsSubtractedIsSmallerThan0AndWithRows() {
    // setup
    int currentStart = ROWS - 5;

    // action
    String prevLink = instance.createPrevResultsAsString(currentStart, ROWS, QUERY_ID, VERSION);

    // verify
    assertThat(prevLink, containsString(String.format("start=%d", 0)));
    assertThat(prevLink, containsString(String.format("rows=%d", ROWS)));
  }

  @Test
  public void createPrevResultsReturnsNullWhenTheCurrentStartIsZero() {
    // setup
    int currentStart = 0;

    // action
    String prevLink = instance.createPrevResultsAsString(currentStart, ROWS, QUERY_ID, VERSION);

    // verify
    assertThat(prevLink, is(nullValue()));
  }

}

