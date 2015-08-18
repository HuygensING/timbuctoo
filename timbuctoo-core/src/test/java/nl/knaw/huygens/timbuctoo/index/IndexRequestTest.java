package nl.knaw.huygens.timbuctoo.index;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import org.junit.Before;
import org.junit.Test;
import test.variation.model.projecta.ProjectADomainEntity;

import java.time.LocalDateTime;

import static nl.knaw.huygens.timbuctoo.index.IndexRequest.Status.DONE;
import static nl.knaw.huygens.timbuctoo.index.IndexRequest.Status.IN_PROGRESS;
import static nl.knaw.huygens.timbuctoo.index.IndexRequest.Status.REQUESTED;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IndexRequestTest {

  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ID = "id";
  private StorageIteratorStub<ProjectADomainEntity> iterator;
  private Repository repository;

  @Before
  public void setup() {
    repository = mock(Repository.class);
  }

  @Test
  public void doneSetsTheLastChangedToNowAndTheStatusToDONE() throws InterruptedException {
    IndexRequest indexRequest = IndexRequest.forType(TYPE);
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
    IndexRequest indexRequest = IndexRequest.forType(TYPE);
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

  @Test
  public void getEntitiesCallsRepositoryGetDomainEntitiesWhenTheClassIsInstantiatedWithForType() {
    // setup
    IndexRequest instance = IndexRequest.forType(TYPE);

    iterator = StorageIteratorStub.newInstance();
    when(repository.getDomainEntities(TYPE)).thenReturn(iterator);

    // action
    StorageIterator<? extends DomainEntity> actualIterator = instance.getEntities(repository);

    // verify
    assertThat(actualIterator, is(sameInstance(iterator)));
    verify(repository).getDomainEntities(TYPE);
  }

  @Test
  public void getEntitiesCallsRepositoryGetEntityOrDefaultVariationWhenTheClassIsInstantiatedWithForEntity() {
    // setup
    IndexRequest instance = IndexRequest.forEntity(TYPE, ID);
    ProjectADomainEntity entity = new ProjectADomainEntity();
    when(repository.getEntityOrDefaultVariation(TYPE, ID)).thenReturn(entity);

    // action
    StorageIterator<? extends DomainEntity> actualIterator = instance.getEntities(repository);

    // verify
    assertThat(actualIterator, is(notNullValue()));
    assertThat(actualIterator.getAll(), contains(entity));
    verify(repository).getEntityOrDefaultVariation(TYPE, ID);
  }

}
