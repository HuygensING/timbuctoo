package nl.knaw.huygens.timbuctoo.datastores.rssource;

import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.berkeleydb.isclean.StringStringIsCleanHandler;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbPatchVersionStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.dropwizard.BdbNonPersistentEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangeListBuilderTest {

  @Test
  public void retrieveChangeFilesNamesReturnsCorrectNamesBasedOnSuppliedVersions() {
    ChangeListBuilder changeListBuilder = new ChangeListBuilder();
    List<String> changeFileNames = changeListBuilder.retrieveChangeFileNames(IntStream.of(1, 2).boxed());

    assertThat(changeFileNames, contains("changes1.nqud", "changes2.nqud"));
  }

  @Test
  public void retrieveChangesReturnsQuadsForGivenVersionAndSubjects() throws Exception {
    UpdatedPerPatchStore updatedPerPatchStore = mock(UpdatedPerPatchStore.class);
    when(updatedPerPatchStore.getVersions()).thenReturn(IntStream.of(1).boxed());

    BdbNonPersistentEnvironmentCreator dataStoreFactory = new BdbNonPersistentEnvironmentCreator();
    BdbPatchVersionStore bdbPatchVersionStore = new BdbPatchVersionStore(dataStoreFactory.getDatabase(
        "user",
        "dataSet",
        "patchVersion",
        false,
        TupleBinding.getPrimitiveBinding(String.class),
        TupleBinding.getPrimitiveBinding(String.class),
        new StringStringIsCleanHandler()
    ));

    bdbPatchVersionStore.put("s1", "p1", Direction.OUT, true, "o1", null, null, null);
    bdbPatchVersionStore.put("s2", "p2", Direction.OUT, false, "o2", null, null, null);
    bdbPatchVersionStore.put("s3", "p3", Direction.OUT, true, "o3", RdfConstants.STRING, null, null);
    bdbPatchVersionStore.put("s4", "p4", Direction.OUT, false, "o4", RdfConstants.STRING, null, null);
    bdbPatchVersionStore.put("s5", "p5", Direction.OUT, true, "o5", RdfConstants.LANGSTRING, "en", null);
    bdbPatchVersionStore.put("s6", "p6", Direction.OUT, false, "o6", RdfConstants.LANGSTRING, "en", null);

    ChangeListBuilder changeListBuilder = new ChangeListBuilder();
    List<String> changes = changeListBuilder
        .retrieveChanges(bdbPatchVersionStore.retrieveChanges())
        .collect(Collectors.toList());

    assertThat(changes, containsInAnyOrder(
      "+<s1> <p1> <o1> .\n",
      "-<s2> <p2> <o2> .\n",
      "+<s3> <p3> \"o3\"^^<http://www.w3.org/2001/XMLSchema#string> .\n",
      "-<s4> <p4> \"o4\"^^<http://www.w3.org/2001/XMLSchema#string> .\n",
      "+<s5> <p5> \"o5\"@en .\n",
      "-<s6> <p6> \"o6\"@en .\n"
    ));
  }
}
