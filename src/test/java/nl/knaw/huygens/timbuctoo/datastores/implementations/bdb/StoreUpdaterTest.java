package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.dataset.ChangeFetcher;
import nl.knaw.huygens.timbuctoo.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.dataset.OptimizedPatchListener;
import nl.knaw.huygens.timbuctoo.dataset.dto.ImportStatusLabel;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.RdfProcessingFailedException;
import nl.knaw.huygens.timbuctoo.filestorage.ChangeLogStorage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StoreUpdaterTest {
  @Test
  public void startAddsProgressForThePrimaryStores() throws Exception {
    ImportStatus importStatus = mock(ImportStatus.class);
    StoreUpdater instance = createInstance(importStatus, Lists.newArrayList());

    instance.start(0);

    verify(importStatus).addProgressItem(BdbPatchVersionStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    verify(importStatus).addProgressItem(BdbQuadStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    verify(importStatus).addProgressItem(BdbTypeNameStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    verify(importStatus).addProgressItem(UpdatedPerPatchStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
    verify(importStatus).addProgressItem(OldSubjectTypesStore.class.getSimpleName(), ImportStatusLabel.IMPORTING);
  }

  @Test
  public void startAddsProgressForDerivedStores() throws Exception {
    ImportStatus importStatus = mock(ImportStatus.class);
    StoreUpdater instance = createInstance(importStatus, Lists.newArrayList(new DerivedStore1(), new DerivedStore2()));

    instance.start(0);

    verify(importStatus).addProgressItem(DerivedStore1.class.getSimpleName(), ImportStatusLabel.PENDING);
    verify(importStatus).addProgressItem(DerivedStore2.class.getSimpleName(), ImportStatusLabel.PENDING);
  }

  @Test
  public void onceEvery5SecondesTheStoreProgressItemsAreUpdated() throws Exception {
    ImportStatus importStatus = mock(ImportStatus.class);
    StoreUpdater instance = createInstance(importStatus, Lists.newArrayList());
    instance.start(0);

    instance.onQuad(true, "", "", "", "", "", "");
    Thread.sleep(6000); // wait 6 seconds to let the second update trigger the notify
    instance.onQuad(true, "", "", "", "", "", "");

    verify(importStatus).updateProgressItem(BdbPatchVersionStore.class.getSimpleName(), 2);
    verify(importStatus).updateProgressItem(BdbQuadStore.class.getSimpleName(), 2);
    verify(importStatus).updateProgressItem(BdbTypeNameStore.class.getSimpleName(), 2);
    verify(importStatus).updateProgressItem(UpdatedPerPatchStore.class.getSimpleName(), 2);
    verify(importStatus).updateProgressItem(OldSubjectTypesStore.class.getSimpleName(), 2);
  }

  @Test
  public void commitSetsTheProgressItemsOfThePrimaryStoresToDone() throws Exception {
    ImportStatus importStatus = mock(ImportStatus.class);
    StoreUpdater instance = createInstance(importStatus, Lists.newArrayList());
    instance.start(0);

    instance.commit();

    verify(importStatus).finishProgressItem(BdbPatchVersionStore.class.getSimpleName());
    verify(importStatus).finishProgressItem(BdbQuadStore.class.getSimpleName());
    verify(importStatus).finishProgressItem(BdbTypeNameStore.class.getSimpleName());
    verify(importStatus).finishProgressItem(UpdatedPerPatchStore.class.getSimpleName());
    verify(importStatus).finishProgressItem(OldSubjectTypesStore.class.getSimpleName());
  }

  @Test
  public void commitProcessesTheDataForTheDerivedStores() throws Exception {
    ImportStatus importStatus = mock(ImportStatus.class);
    StoreUpdater instance = createInstance(importStatus, Lists.newArrayList(new DerivedStore1(), new DerivedStore2()));

    instance.start(0);
    instance.commit();

    verify(importStatus).startProgressItem(DerivedStore1.class.getSimpleName());
    verify(importStatus).startProgressItem(DerivedStore2.class.getSimpleName());
    verify(importStatus).finishProgressItem(DerivedStore1.class.getSimpleName());
    verify(importStatus).finishProgressItem(DerivedStore2.class.getSimpleName());
  }

  private StoreUpdater createInstance(ImportStatus importStatus, List<OptimizedPatchListener> listeners)
      throws IOException {
    BdbTypeNameStore bdbTypeNameStore = mock(BdbTypeNameStore.class);
    BdbQuadStore bdbQuadStore = mock(BdbQuadStore.class);
    GraphStore graphStore = mock(GraphStore.class);
    BdbPatchVersionStore patchVersionStore = mock(BdbPatchVersionStore.class);
    UpdatedPerPatchStore updatedPerPatchStore = mock(UpdatedPerPatchStore.class);
    OldSubjectTypesStore oldSubjectTypesStore = mock(OldSubjectTypesStore.class);
    ChangeLogStorage changeLogStorage = mock(ChangeLogStorage.class);
    given(changeLogStorage.getChangeLogOutputStream(anyInt())).willAnswer(inv -> OutputStream.nullOutputStream());

    return new StoreUpdater(
      bdbQuadStore,
      graphStore,
      bdbTypeNameStore,
      patchVersionStore,
      updatedPerPatchStore,
      oldSubjectTypesStore,
      listeners,
      importStatus,
      changeLogStorage
    );
  }

  private static class DerivedStore1 implements OptimizedPatchListener {
    @Override
    public void start() throws RdfProcessingFailedException {
    }

    @Override
    public void onChangedSubject(String subject, ChangeFetcher changeFetcher) throws RdfProcessingFailedException {
    }

    @Override
    public void notifyUpdate() {
    }

    @Override
    public void finish() throws RdfProcessingFailedException {
    }
  }

  private static class DerivedStore2 implements OptimizedPatchListener {
    @Override
    public void start() throws RdfProcessingFailedException {
    }

    @Override
    public void onChangedSubject(String subject, ChangeFetcher changeFetcher) throws RdfProcessingFailedException {
    }

    @Override
    public void notifyUpdate() {
    }

    @Override
    public void finish() throws RdfProcessingFailedException {
    }
  }
}
