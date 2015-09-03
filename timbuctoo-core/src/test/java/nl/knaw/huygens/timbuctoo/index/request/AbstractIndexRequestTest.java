package nl.knaw.huygens.timbuctoo.index.request;

import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.Indexer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import test.variation.model.projecta.ProjectADomainEntity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractIndexRequestTest {
  protected static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  protected Indexer indexer;
  protected IndexRequestStatus requestedStatus;
  private IndexRequest instance;
  private IndexRequestStatus doneStatus;
  private IndexRequestStatus inProgressStatus;
  public static final int TIMEOUT = 1000;

  @Before
  public void setup() {
    setupStatuses();
    instance = createInstance();
    indexer = mock(Indexer.class);
  }

  protected IndexRequest getInstance() {
    return instance;
  }

  protected abstract IndexRequest createInstance();

  @Test
  public void executeSetsTheStatusToInProgressPriorToIndexing() throws Exception {
    // action
    getInstance().execute(indexer);

    // verify
    InOrder inOrder = inOrder(indexer, requestedStatus);
    inOrder.verify(requestedStatus).inProgress();
    verifyIndexAction(inOrder);

  }

  protected void setupStatuses() {
    doneStatus = mock(IndexRequestStatus.class);
    when(doneStatus.getStatus()).thenReturn(IndexRequest.Status.DONE);

    inProgressStatus = mock(IndexRequestStatus.class);
    when(inProgressStatus.done()).thenReturn(doneStatus);

    requestedStatus = mock(IndexRequestStatus.class);
    when(requestedStatus.inProgress()).thenReturn(inProgressStatus);
  }

  protected abstract void verifyIndexAction(InOrder inOrder) throws IndexException;

  @Test
  public void executeSetsTheStatusToDoneAfterIndexing() throws Exception {
    // setup
    IndexRequest instance = getInstance();

    // action
    instance.execute(indexer);

    // verify
    assertThat(instance.getStatus(), is(IndexRequest.Status.DONE));

    InOrder inOrder = inOrder(indexer, inProgressStatus);
    verifyIndexAction(inOrder);
    inOrder.verify(inProgressStatus).done();
  }

  @Test
  public void canBeDiscardedReturnsTrueIfTheStatusIsDoneAndTheAndTheTimeoutHasPassedAfterTheLastChangedDate() throws Exception {
    // setup
    IndexRequest instance = getInstance();
    instance.execute(indexer); // sets the status to done

    Thread.sleep(TIMEOUT + 1); // wait for the timeout to pass

    // action
    boolean readyForPurge = instance.canBeDiscarded(TIMEOUT);

    // verify
    assertThat(readyForPurge, is(true));
  }

  @Test
  public void canBeDiscardedReturnsFalseIfTheStatusIsNotDone() throws Exception {
    // setup
    IndexRequest instance = getInstance();

    // action
    boolean readyForPurge = instance.canBeDiscarded(TIMEOUT);

    // verify
    assertThat(readyForPurge, is(false));
  }

  @Test
  public void canBeDiscardedReturnsFalseWhenTheTimeoutIsNotPassed() throws Exception {
    // setup
    IndexRequest instance = getInstance();
    instance.execute(indexer); // sets the status to done

    // action
    boolean readyForPurge = instance.canBeDiscarded(TIMEOUT);

    // verify
    assertThat(readyForPurge, is(false));
  }
}
