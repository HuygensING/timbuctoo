package nl.knaw.huygens.timbuctoo.search;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.Timeout;
import org.apache.tinkerpop.gremlin.structure.Graph;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchStoreTest {

  private static final Graph NULL_GRAPH = null;
  public static final SearchResult SEARCH_RESULT = new SearchResult(null, null, null, null);
  public static final SearchResult OTHER_SEARCH_RESULT1 = new SearchResult(null, null, null, null);
  private static final Timeout ONE_SECOND_TIMEOUT = new Timeout(1, TimeUnit.SECONDS);
  private SearchStore instance;

  @Before
  public void setUp() throws Exception {
    GraphWrapper graphWrapper = mock(GraphWrapper.class);
    when(graphWrapper.getGraph()).thenReturn(NULL_GRAPH);

    instance = new SearchStore(ONE_SECOND_TIMEOUT);
  }

  @Test
  public void addReturnsAnId() {
    UUID id = instance.add(SEARCH_RESULT);

    assertThat(id, is(notNullValue()));
  }

  @Test
  public void getSearchResultReturnsASearchResult() {
    UUID id = instance.add(SEARCH_RESULT);

    Optional<SearchResult> searchResult = instance.getSearchResult(id);

    assertThat(searchResult, is(present()));
  }

  @Test
  public void searchCreatesADifferentIdEachCall() {
    UUID id1 = instance.add(SEARCH_RESULT);
    UUID id2 = instance.add(SEARCH_RESULT);

    assertThat(id1, is(not(id2)));
  }

  @Test
  public void getSearchResultReturnsTheSameResultForTheSameId() {
    UUID id = instance.add(SEARCH_RESULT);

    SearchResult retrieval1 = instance.getSearchResult(id).get();
    SearchResult retrieval2 = instance.getSearchResult(id).get();

    assertThat(retrieval1, is(retrieval2));
  }

  @Test
  public void getSearchResultReturnsADifferentResultForDifferentIds() {
    UUID id1 = instance.add(SEARCH_RESULT);
    UUID id2 = instance.add(OTHER_SEARCH_RESULT1);

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
    UUID id = instance.add(SEARCH_RESULT);

    Optional<SearchResult> searchResult = instance.getSearchResult(id);
    assertThat(searchResult, is(present()));

    Thread.sleep(ONE_SECOND_TIMEOUT.toMilliseconds() + 1);

    Optional<SearchResult> searchResultAfterTimeout = instance.getSearchResult(id);
    assertThat(searchResultAfterTimeout, is(not(present())));
  }

}
