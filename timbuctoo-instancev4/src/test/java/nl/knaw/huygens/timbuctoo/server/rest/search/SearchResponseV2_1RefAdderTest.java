package nl.knaw.huygens.timbuctoo.server.rest.search;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.server.search.EntityRef;
import org.junit.Test;

import java.util.HashMap;

import static nl.knaw.huygens.timbuctoo.server.rest.search.SearchResponseV2_1RefMatcher.likeSearchResponseRef;
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

    assertThat(searchResponse.getRefs(), contains(
      likeSearchResponseRef()
        .withId("id")
        .withType("type")
        .withDisplayName("displayName")
        .withPath("v2.1/domain/types/id")));
  }

  @Test
  public void addsTheDataOfTheEntityRefToTheSearchResponseV2_1Ref() {
    SearchResponseV2_1RefAdder instance = new SearchResponseV2_1RefAdder();

    SearchResponseV2_1 searchResponse = new SearchResponseV2_1();

    EntityRef entityRef = new EntityRef("type", "id");
    HashMap<String, Object> data = Maps.newHashMap();
    entityRef.setData(data);

    instance.addRef(searchResponse, entityRef);

    assertThat(searchResponse.getRefs(), contains(likeSearchResponseRef()
      .withData(data)));
  }
}
