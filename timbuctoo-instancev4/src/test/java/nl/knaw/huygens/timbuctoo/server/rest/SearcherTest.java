package nl.knaw.huygens.timbuctoo.server.rest;

import nl.knaw.huygens.timbuctoo.util.Timeout;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static nl.knaw.huygens.timbuctoo.util.OptionalPresentMatcher.present;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class SearcherTest {

  private TimbuctooQuery query;
  public static final Timeout ONE_SECOND_TIMEOUT = new Timeout(1, TimeUnit.SECONDS);
  private Searcher instance;

  @Before
  public void setUp() throws Exception {
    query = mock(TimbuctooQuery.class);
    // makes sure a new search result is created with each invocation
    given(query.execute()).willAnswer(invocation -> mock(SearchResult.class));

    instance = new Searcher(ONE_SECOND_TIMEOUT);
  }

  @Test
  public void searchReturnsAnId() {
    UUID id = instance.search(query);

    assertThat(id, is(notNullValue()));
  }

  @Test
  public void getSearchResultReturnsASearchResult() {
    UUID id = instance.search(query);

    Optional<SearchResult> searchResult = instance.getSearchResult(id);

    assertThat(searchResult, is(present()));
  }

  @Test
  public void searchCreatesADifferentIdEachCall() {
    UUID id1 = instance.search(query);
    UUID id2 = instance.search(query);

    assertThat(id1, is(not(id2)));
  }

  @Test
  public void getSearchResultReturnsTheSameResultForTheSameId() {
    UUID id = instance.search(query);

    SearchResult retrieval1 = instance.getSearchResult(id).get();
    SearchResult retrieval2 = instance.getSearchResult(id).get();

    assertThat(retrieval1, is(retrieval2));
  }

  @Test
  public void getSearchResultReturnsADifferentResultForDifferentIds() {
    UUID id1 = instance.search(query);
    UUID id2 = instance.search(query);

    SearchResult searchResult1 = instance.getSearchResult(id1).get();
    SearchResult searchResult2 = instance.getSearchResult(id2).get();

    assertThat(searchResult1, is(not(searchResult2)));
  }

  @Test
  public void getSearchResultReturnsNullIfTheSearchResultIsNotFound() {
    Optional<SearchResult> searchResult = instance.getSearchResult(UUID.randomUUID());

    assertThat(searchResult, is(not(present())));
  }

  @Test
  public void getSearchResultReturnsNullIfTheSearchAfterTheTimeOut() throws InterruptedException {
    UUID id = instance.search(query);

    Optional<SearchResult> searchResult = instance.getSearchResult(id);
    assertThat(searchResult, is(present()));

    Thread.sleep(ONE_SECOND_TIMEOUT.toMilliseconds() + 1);

    Optional<SearchResult> searchResultAfterTimeout = instance.getSearchResult(id);
    assertThat(searchResultAfterTimeout, is(not(present())));
  }

}
