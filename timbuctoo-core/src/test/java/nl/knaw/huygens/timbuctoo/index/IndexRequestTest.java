package nl.knaw.huygens.timbuctoo.index;

import org.junit.Test;
import test.variation.model.projecta.ProjectADomainEntity;

import java.time.LocalDateTime;

import static nl.knaw.huygens.timbuctoo.index.IndexRequest.Status.DONE;
import static nl.knaw.huygens.timbuctoo.index.IndexRequest.Status.IN_PROGRESS;
import static nl.knaw.huygens.timbuctoo.index.IndexRequest.Status.REQUESTED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IndexRequestTest {
  @Test
  public void doneSetsTheLastChangedToNowAndTheStatusToDONE() throws InterruptedException {
    IndexRequest indexRequest = IndexRequest.forType(ProjectADomainEntity.class);
    LocalDateTime created = indexRequest.getLastChanged();
    verifyStatus(indexRequest, REQUESTED);
    Thread.sleep(100);

    // action
    indexRequest.done();

    LocalDateTime done = indexRequest.getLastChanged();

    Thread.sleep(100);
    LocalDateTime afterDone = LocalDateTime.now();

    // verify
    verifyStatus(indexRequest, DONE);
    assertThat("done.isAfter(created)", done.isAfter(created), is(true));
    assertThat("done.isBefore(afterDone)", done.isBefore(afterDone), is(true));
  }

  private void verifyStatus(IndexRequest indexRequest, IndexRequest.Status expectedStatus) {
    assertThat(indexRequest.getStatus(), is(expectedStatus));
  }

  @Test
  public void inProgressSetsTheLastChangedToNowAndTheStatusToIN_PROGRESS() throws InterruptedException {
    IndexRequest indexRequest = IndexRequest.forType(ProjectADomainEntity.class);
    LocalDateTime created = indexRequest.getLastChanged();
    verifyStatus(indexRequest, REQUESTED);
    Thread.sleep(100);

    // action
    indexRequest.inProgress();

    LocalDateTime inProgress = indexRequest.getLastChanged();

    Thread.sleep(100);
    LocalDateTime afterInProgress = LocalDateTime.now();

    // verify
    verifyStatus(indexRequest, IN_PROGRESS);
    assertThat("inProgress.isAfter(created)", inProgress.isAfter(created), is(true));
    assertThat("inProgress.isBefore(afterInProgress)", inProgress.isBefore(afterInProgress), is(true));
  }

}
