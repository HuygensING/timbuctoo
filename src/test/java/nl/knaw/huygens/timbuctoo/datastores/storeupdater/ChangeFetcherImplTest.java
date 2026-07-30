package nl.knaw.huygens.timbuctoo.datastores.storeupdater;

import nl.knaw.huygens.timbuctoo.dataset.StoreProvider;
import nl.knaw.huygens.timbuctoo.datastores.patchversionstore.PatchVersionStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.QuadGraphs;
import nl.knaw.huygens.timbuctoo.datastores.updatedperpatchstore.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.dropwizard.BdbNonPersistentEnvironmentCreator;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction.OUT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class ChangeFetcherImplTest {
  @Test
  public void showsAdditions() throws Exception {
    final BdbNonPersistentEnvironmentCreator databaseCreator = new BdbNonPersistentEnvironmentCreator();
    final StoreProvider storeProvider = databaseCreator.createStoreProvider("a", "b");
    final QuadStore bdbQuadStore = storeProvider.createQuadStore();
    final PatchVersionStore patchVersionStore = storeProvider.createPatchVersionStore();

    bdbQuadStore.putQuad("subj", "pred", OUT, "obj", null, null, null);
    patchVersionStore.put("subj", "pred", OUT, true, "obj", null, null, null);

    ChangeFetcherImpl changeFetcher = new ChangeFetcherImpl(patchVersionStore, bdbQuadStore);

    try (Stream<QuadGraphs> predicates = changeFetcher.getPredicates("subj", "pred", OUT, false, false, true)) {
      assertThat(predicates.count(), is(1L));
    }
    try (Stream<QuadGraphs> predicates = changeFetcher.getPredicates("subj", "pred", OUT, true, true, true)) {
      assertThat(predicates.count(), is(1L));
    }
    try (Stream<QuadGraphs> predicates = changeFetcher.getPredicates("subj", "pred", OUT, true, false, true)) {
      assertThat(predicates.count(), is(1L));
    }

    patchVersionStore.empty();
    bdbQuadStore.putQuad("subj", "pred", OUT, "obj2", null, null, "graph");
    patchVersionStore.put("subj", "pred", OUT, true, "obj2", null, null, "graph");
    changeFetcher = new ChangeFetcherImpl(patchVersionStore, bdbQuadStore);

    try (Stream<QuadGraphs> predicates = changeFetcher.getPredicates("subj", "pred", OUT, false, false, true)) {
      assertThat(predicates.count(), is(1L));
    }
    try (Stream<QuadGraphs> predicates = changeFetcher.getPredicates("subj", "pred", OUT, true, true, true)) {
      assertThat(predicates.count(), is(2L));
    }
    try (Stream<QuadGraphs> predicates = changeFetcher.getPredicates("subj", "pred", OUT, true, false, true)) {
      assertThat(predicates.count(), is(1L));
    }
  }
}
