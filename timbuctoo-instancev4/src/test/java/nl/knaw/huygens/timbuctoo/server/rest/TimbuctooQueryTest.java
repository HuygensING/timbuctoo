package nl.knaw.huygens.timbuctoo.server.rest;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class TimbuctooQueryTest {

  public static final WwPersonSearchDescription DESCRIPTION = new WwPersonSearchDescription();

  @Test
  public void executeReturnsASearchResult() {
    TimbuctooQuery instance = new TimbuctooQuery(DESCRIPTION);

    SearchResult searchResult = instance.execute();

    assertThat(searchResult, is(notNullValue()));
  }

}
