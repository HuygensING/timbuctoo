package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.index.Indexer;
import org.junit.Before;
import org.junit.Test;
import test.variation.model.projecta.ProjectADomainEntity;

import java.time.LocalDateTime;

import static nl.knaw.huygens.timbuctoo.index.request.IndexRequest.Status.DONE;
import static nl.knaw.huygens.timbuctoo.index.request.IndexRequest.Status.IN_PROGRESS;
import static nl.knaw.huygens.timbuctoo.index.request.IndexRequest.Status.REQUESTED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public abstract class AbstractIndexRequestTest {
  protected static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  protected Indexer indexer;
  private IndexRequest instance;

  @Before
  public void setup() {
    instance = createInstance();
    indexer = mock(Indexer.class);
  }

  @Test
  public void doneSetsTheLastChangedToNowAndTheStatusToDONE() throws InterruptedException {
    LocalDateTime created = getInstance().getLastChanged();
    verifyStatus(getInstance(), REQUESTED);
    Thread.sleep(100);

    // action
    getInstance().done();

    LocalDateTime done = getInstance().getLastChanged();

    Thread.sleep(100);
    LocalDateTime afterDone = LocalDateTime.now();

    // verify
    verifyStatus(getInstance(), DONE);
    assertThat("done.isAfter(created)", done.isAfter(created), is(true));
    assertThat("done.isBefore(afterDone)", done.isBefore(afterDone), is(true));
  }

  protected IndexRequest getInstance() {
    return instance;
  }

  protected abstract IndexRequest createInstance();

  protected void verifyStatus(IndexRequest indexRequest, EntityIndexRequest.Status expectedStatus) {
    assertThat(indexRequest.getStatus(), is(expectedStatus));
  }

  @Test
  public void inProgressSetsTheLastChangedToNowAndTheStatusToIN_PROGRESS() throws InterruptedException {
    LocalDateTime created = getInstance().getLastChanged();
    verifyStatus(getInstance(), REQUESTED);
    Thread.sleep(100);

    // action
    getInstance().inProgress();

    LocalDateTime inProgress = getInstance().getLastChanged();

    Thread.sleep(100);
    LocalDateTime afterInProgress = LocalDateTime.now();

    // verify
    verifyStatus(getInstance(), IN_PROGRESS);
    assertThat("inProgress.isAfter(created)", inProgress.isAfter(created), is(true));
    assertThat("inProgress.isBefore(afterInProgress)", inProgress.isBefore(afterInProgress), is(true));
  }
}
