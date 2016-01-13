package nl.knaw.huygens.timbuctoo.server.rest;

import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.server.rest.SearchResponseV2_1Matcher.likeSearchResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SearchResponseV2_1Test {

  public static final WwPersonSearchDescription DESCRIPTION = new WwPersonSearchDescription();

  @Test
  public void fromReturnsASearchResponseV2_1WithTheFullTextSearchFieldsOfTheDescription() {
    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION);

    assertThat(searchResponse, is(likeSearchResponse()
      .withFullTextSearchFields(DESCRIPTION.getFullTextSearchFields())));
  }

  @Test
  public void fromReturnsASearchResponseV2_1WithTheSortableFieldsOfTheDescription() {
    SearchResponseV2_1 searchResponse = SearchResponseV2_1.from(DESCRIPTION);

    assertThat(searchResponse, is(likeSearchResponse()
      .withSortableFields(DESCRIPTION.getSortableFields())));
  }

}
