package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ChangesListBuilderTest {

  @Test
  public void retrieveChangeFilesNamesReturnsCorrectNamesBasedOnSuppliedVersions() {
    UpdatedPerPatchStore updatedPerPatchStore = mock(UpdatedPerPatchStore.class);
    BdbTruePatchStore bdbTruePatchStore = mock(BdbTruePatchStore.class);

    ChangeListBuilder changeListBuilder = new ChangeListBuilder(updatedPerPatchStore,bdbTruePatchStore, "graph");

    Supplier<List<Integer>> versionsSupplier = () -> Lists.newArrayList(1, 2);

    List<String> changeFileNames = changeListBuilder.retrieveChangeFileNames(versionsSupplier);

    assertThat(changeFileNames, contains("changes1.nqud", "changes2.nqud"));
  }

  @Test
  public void retrieveChangesReturnsQuadsForGivenVersionAndSubjects() {
    UpdatedPerPatchStore updatedPerPatchStore = mock(UpdatedPerPatchStore.class);
    BdbTruePatchStore bdbTruePatchStore = mock(BdbTruePatchStore.class);

    CursorQuad cursorQuad = mock(CursorQuad.class);
    given(cursorQuad.getSubject()).willReturn("s1");
    given(cursorQuad.getPredicate()).willReturn("p1");
    given(cursorQuad.getChangeType()).willReturn(ChangeType.ASSERTED);
    given(cursorQuad.getObject()).willReturn("o1");
    given(cursorQuad.getDirection()).willReturn(Direction.OUT);

    given(bdbTruePatchStore.getChanges("s1",1,true)).willReturn(Stream.of(cursorQuad));

    ChangeListBuilder changeListBuilder = new ChangeListBuilder(updatedPerPatchStore,bdbTruePatchStore, "graph");

    Integer version = 1;

    Supplier<List<String>> subjectsSupplier = () -> Lists.newArrayList("s1");

    List<String> changes = changeListBuilder.retrieveChanges(version,subjectsSupplier);

    assertThat(changes, contains("+<s1> <p1> <o1> <graph> .\n"));
  }
}
