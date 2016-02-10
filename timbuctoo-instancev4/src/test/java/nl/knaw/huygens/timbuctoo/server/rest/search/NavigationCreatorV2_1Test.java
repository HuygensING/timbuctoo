package nl.knaw.huygens.timbuctoo.server.rest.search;

import nl.knaw.huygens.timbuctoo.server.SearchConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class NavigationCreatorV2_1Test {

  private NavigationCreator instance;
  private SearchConfig searchConfig;

  @Before
  public void setUp() throws Exception {
    searchConfig = mock(SearchConfig.class);
    instance = new NavigationCreator(searchConfig);
  }

  @Test
  public void nextAddsANextLinkToTheSearchResult() {
    SearchResponseV2_1 searchResponse = new SearchResponseV2_1();
    UUID id = UUID.fromString("17baf00d-0089-4446-a9bd-2a4add3e55ea");

    instance.next(searchResponse, 10, 0, 20, id);

    assertThat(searchResponse,
      hasProperty("next", endsWith("/v2.1/search/17baf00d-0089-4446-a9bd-2a4add3e55ea?start=10&rows=10")));
  }

  @Test
  public void nextAddsNoNextLinkWhenTheCurrentStartAndRowsIsLargerThanNumFound() {
    SearchResponseV2_1 searchResponse = new SearchResponseV2_1();
    UUID id = UUID.fromString("17baf00d-0089-4446-a9bd-2a4add3e55ea");

    instance.next(searchResponse, 10, 0, 8, id);

    assertThat(searchResponse, hasProperty("next", is(nullValue())));
  }

  @Test
  public void nextAddsNoNextLinkWhenTheCurrentStartAndRowsIsEqualToNumFound() {
    SearchResponseV2_1 searchResponse = new SearchResponseV2_1();
    UUID id = UUID.fromString("17baf00d-0089-4446-a9bd-2a4add3e55ea");

    instance.next(searchResponse, 10, 0, 10, id);

    assertThat(searchResponse, hasProperty("next", is(nullValue())));
  }

  @Test
  public void nextAddsAUrlThatStartsWithTheBaseUrlFromTheSearchConfig() {
    SearchResponseV2_1 searchResponse = new SearchResponseV2_1();
    UUID id = UUID.fromString("17baf00d-0089-4446-a9bd-2a4add3e55ea");
    given(searchConfig.getBaseUri()).willReturn("http://example.com");

    instance.next(searchResponse, 10, 0, 20, id);

    assertThat(searchResponse, hasProperty("next", startsWith("http://example.com")));
  }

}
