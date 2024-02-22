package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.berkeleydb.isclean.StringStringIsCleanHandler;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.QuadGraphs;
import nl.knaw.huygens.timbuctoo.dropwizard.BdbNonPersistentEnvironmentCreator;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction.OUT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangeFetcherImplTest {
  @Test
  public void showsAdditions() throws Exception {
    UpdatedPerPatchStore updatedPerPatchStore = mock(UpdatedPerPatchStore.class);
    when(updatedPerPatchStore.getVersions()).thenReturn(IntStream.of(0, 1).boxed());

    final BdbNonPersistentEnvironmentCreator databaseCreator = new BdbNonPersistentEnvironmentCreator();
    final BdbQuadStore bdbQuadStore = new BdbQuadStore(databaseCreator.getDatabase(
      "a",
      "b",
      "rdfData",
      true,
      TupleBinding.getPrimitiveBinding(String.class),
      TupleBinding.getPrimitiveBinding(String.class),
      new StringStringIsCleanHandler()
    ));
    final BdbPatchVersionStore patchVersionStore = new BdbPatchVersionStore(databaseCreator.getDatabase(
      "a",
      "b",
      "patchVersion",
      true,
      TupleBinding.getPrimitiveBinding(String.class),
      TupleBinding.getPrimitiveBinding(String.class),
      new StringStringIsCleanHandler()
    ));

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
