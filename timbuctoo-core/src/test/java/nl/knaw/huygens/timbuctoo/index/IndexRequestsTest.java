package nl.knaw.huygens.timbuctoo.index;

import com.google.common.cache.Cache;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import test.variation.model.projecta.ProjectADomainEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;


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
    indexRequest = IndexRequest.forType(TYPE);
    instance = new IndexRequests(TIMEOUT);
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
    String id = instance.add(indexRequest);
    IndexRequest beforeDone = instance.get(id);
    assertThat(beforeDone, is(sameInstance(this.indexRequest)));
    beforeDone.done();

    Thread.sleep(TIMEOUT);

    // action
    instance.add(IndexRequest.forType(TYPE));

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
    IndexRequest beforeDone = instance.get(id);
    beforeDone.done();

    // wait until the request has timed out
    Thread.sleep(TIMEOUT);

    // action
    IndexRequest foundForLastTime = instance.get(id);

    // verify
    assertThat(foundForLastTime, is(sameInstance(indexRequest)));
    IndexRequest notFoundAgain = instance.get(id);
    assertThat(notFoundAgain, is(nullValue()));
  }

}
