package nl.knaw.huygens.timbuctoo.index;

import com.google.common.cache.Cache;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.when;


public class IndexRequestsTest {

  @Mock
  private Cache<String, IndexRequest> cacheMock;
  private IndexRequests instance;
  public static final IndexRequest INDEX_REQUEST = IndexRequest.indexAll();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    instance = new IndexRequests();

  }

  @Test
  public void addMakesItPossibleToRequestTheRequest() {
    // action
    String id = instance.add(INDEX_REQUEST);

    // verify
    assertThat(instance.get(id), is(sameInstance(INDEX_REQUEST)));

  }

  @Test
  public void getReturnsNullWhenNotIndexRequestIsFound() throws Exception {
    // setup
    String nonExistingId = "nonExistingId";

    // action
    IndexRequest indexRequest = instance.get(nonExistingId);

    // verify
    assertThat(indexRequest, is(nullValue()));
  }

  private void noIndexRequestFoundFor(String id) {
    when(cacheMock.getIfPresent(id)).thenReturn(null);
  }

}
