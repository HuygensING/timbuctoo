package nl.knaw.huygens.timbuctoo.rest.util;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.config.Paths.SEARCH_PATH;
import static nl.knaw.huygens.timbuctoo.config.Paths.V1_PATH;
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

  @Before
  public void setup() {
    Configuration config = mock(Configuration.class);
    when(config.getSetting(HATEOASURICreator.PUBLIC_URL)).thenReturn(PUBLIC_URL);
    instance = new HATEOASURICreator(config);
  }

  private HATEOASURICreator instance;

  @Test
  public void createHATEOASURICreatesABaseURIFromThePublicURLTheV1PathAndSearchPathAndAddsTheQueryIdAndQueryParametersStartAndRows() {
    // action
    String hateoasuri = instance.createHATEOASURIAsString(START, ROWS, QUERY_ID);

    // verify
    assertThat(hateoasuri, is(String.format("%s/%s/%s/%s?start=%d&rows=%d", PUBLIC_URL, V1_PATH, SEARCH_PATH, QUERY_ID, START, ROWS)));
  }

  @Test
  public void createNextResultsAsStringReturnsTheCurrentStartAddedWithNumberOfRowsIsTotalFoundIsMore() {
    // setup
    int numfoud = 10 * ROWS;

    // action
    String nextLink = instance.createNextResultsAsString(START, ROWS, numfoud, QUERY_ID);

    //
    assertThat(nextLink, containsString(String.format("start=%d", NEXT_START)));
    assertThat(nextLink, containsString(String.format("rows=%d", ROWS)));
  }

  @Test
  public void createNextResultsAsStringReturnsTheCurrentStartAddedWithNumberOfRowsIsTotalFoundIsEqual() {
    // setup
    int numfoud = 2 * ROWS;

    // action
    String nextLink = instance.createNextResultsAsString(START, ROWS, numfoud, QUERY_ID);

    //
    assertThat(nextLink, containsString(String.format("start=%d", NEXT_START)));
    assertThat(nextLink, containsString(String.format("rows=%d", ROWS)));
  }

  @Test
  public void createNextResultsReturnsNullIfStartAddedWithNumberRowsIsMoreThanTotalFound() {
    // setup
    int numfoud = ROWS - 1;

    // action
    String nextLink = instance.createNextResultsAsString(START, ROWS, numfoud, QUERY_ID);

    // verify
    assertThat(nextLink, is(nullValue()));
  }

  @Test
  public void createNextResultsReturnsNullIfStartAddedWithNumberRowsIsEqualToTotalFound() {
    // setup
    int numfoud = ROWS;

    // action
    String nextLink = instance.createNextResultsAsString(START, ROWS, numfoud, QUERY_ID);

    // verify
    assertThat(nextLink, is(nullValue()));
  }

  @Test
  public void createNextResultsReturnsTheNextStartAndTheNumberOfRows() {
    // setup
    int numfoud = ROWS + 5;

    // action
    String nextLink = instance.createNextResultsAsString(START, ROWS, numfoud, QUERY_ID);

    // verify
    assertThat(nextLink, containsString(String.format("rows=%d", ROWS)));
  }

  @Test
  public void createPrevResultsReturnsTheCurrentStartWithTheRowsSubtractedAndTheRows() {
    // setup
    int currentStart = ROWS;

    // action
    String prevLink = instance.createPrevResultsAsString(currentStart, ROWS, QUERY_ID);

    // verify
    assertThat(prevLink, containsString(String.format("start=%d", currentStart - ROWS)));
    assertThat(prevLink, containsString(String.format("rows=%d", ROWS)));
  }

  @Test
  public void createPrevResultsReturnsThe0IfTheCurrentStartWithTheRowsSubtractedIsSmallerThan0AndWithRows() {
    // setup
    int currentStart = ROWS - 5;

    // action
    String prevLink = instance.createPrevResultsAsString(currentStart, ROWS, QUERY_ID);

    // verify
    assertThat(prevLink, containsString(String.format("start=%d", 0)));
    assertThat(prevLink, containsString(String.format("rows=%d", ROWS)));
  }

}

