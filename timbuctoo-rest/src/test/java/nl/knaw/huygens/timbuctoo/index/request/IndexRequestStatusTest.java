package nl.knaw.huygens.timbuctoo.index.request;

import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.index.request.IndexRequest.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class IndexRequestStatusTest {

  @Test
  public void requestedCreatesAnIndexRequestStatusWithTheStatusRequested(){
    // action
    IndexRequestStatus requested = IndexRequestStatus.requested();

    // verify
    assertThat(requested.getStatus(), is(REQUESTED));
  }

  @Test
  public void inProgressReturnsAnIndexRequestStatusWithTheStatusInProgress(){
    // setup
    IndexRequestStatus requested = IndexRequestStatus.requested();

    // action
    IndexRequestStatus inProgress = requested.inProgress();

    // verify
    assertThat(inProgress.getStatus(), is(IN_PROGRESS));
  }

  @Test
  public void doneReturnsAnIndexRequestStatusWithTheStatusDone(){
    // setup
    IndexRequestStatus requested = IndexRequestStatus.requested();

    // action
    IndexRequestStatus done = requested.done();

    // verify
    assertThat(done.getStatus(), is(DONE));
  }

}
