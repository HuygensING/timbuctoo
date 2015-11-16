package nl.knaw.huygens.timbuctoo.index.request;

import com.google.common.cache.Cache;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import test.rest.model.projecta.ProjectADomainEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class IndexRequestsTest {

  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  @Mock
  private Cache<String, IndexRequest> cache;

  private IndexRequest indexRequest;
  private static final int TIMEOUT = 100;
  private IndexRequests instance;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    indexRequest = createIndexRequest();
    instance = new IndexRequests(TIMEOUT);
  }

  private IndexRequest createIndexRequest() {
    IndexRequest indexRequest = mock(IndexRequest.class);
    when(indexRequest.canBeDiscarded(TIMEOUT)).thenReturn(false, true); // make sure the item is not purged directly after adding
    return  indexRequest;
  }

  @Test
  public void addMakesItPossibleToRequestTheRequest() {
    // action
    String id = instance.add(indexRequest);

    // verify
    assertThat(instance.get(id), is(sameInstance(indexRequest)));

  }

  @Test
  public void addInvalidatesDoneRequestsThatAreChangedMoreThanTheTimeoutAge() throws Exception {
    // setup
    IndexRequest otherIndexRequest = createIndexRequest();

    String id = instance.add(indexRequest);
    verifyRequestCanBeRetreived(id);

    // action
    instance.add(otherIndexRequest);

    // verify
    IndexRequest notFoundAfterTimeout = instance.get(id);
    assertThat(notFoundAfterTimeout, is(nullValue()));

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

  @Test
  public void getInvalidatesDoneRequestsThatAreChangedMoreThanTheTimeoutAge() throws Exception {
    // setup
    String id = instance.add(indexRequest);
    verifyRequestCanBeRetreived(id);

    // action
    IndexRequest notFoundAgain = instance.get(id);

    // verify
    assertThat(notFoundAgain, is(nullValue()));
  }

  protected void verifyRequestCanBeRetreived(String id) {
    IndexRequest beforePurge = instance.get(id);
    assertThat(beforePurge, is(sameInstance(indexRequest)));
  }

}
