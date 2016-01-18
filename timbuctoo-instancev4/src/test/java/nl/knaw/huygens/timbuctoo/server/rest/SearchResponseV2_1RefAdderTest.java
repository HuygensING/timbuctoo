package nl.knaw.huygens.timbuctoo.server.rest;

import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.server.rest.SearchResponseV2_1RefMatcher.likeSearchResponseRef;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class SearchResponseV2_1RefAdderTest {

  @Test
  public void addsASearchResponseRefToTheSearchResult() {
    SearchResponseV2_1RefAdder instance = new SearchResponseV2_1RefAdder();

    SearchResponseV2_1 searchResponse = new SearchResponseV2_1();

    EntityRef entityRef = new EntityRef("type", "id");
    entityRef.setDisplayName("displayName");

    instance.addRef(searchResponse, entityRef);

    assertThat(searchResponse.getRefs(), contains(likeSearchResponseRef()
      .withId("id")
      .withType("type")
      .withDisplayName("displayName")
      .withPath("v2.1/domain/types/id")));
  }
}
