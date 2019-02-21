package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.dataset.ChangesRetriever;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.ChangeType;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ChangeListBuilderTest {

  @Test
  public void retrieveChangeFilesNamesReturnsCorrectNamesBasedOnSuppliedVersions() {
    ChangeListBuilder changeListBuilder = new ChangeListBuilder("graph");

    Supplier<List<Integer>> versionsSupplier = () -> Lists.newArrayList(1, 2);

    List<String> changeFileNames = changeListBuilder.retrieveChangeFileNames(versionsSupplier);

    assertThat(changeFileNames, contains("changes1.nqud", "changes2.nqud"));
  }

  @Test
  public void retrieveChangesReturnsQuadsForGivenVersionAndSubjects() {
    BdbTruePatchStore bdbTruePatchStore = mock(BdbTruePatchStore.class);

    CursorQuad cursorQuad1 = mock(CursorQuad.class);
    given(cursorQuad1.getSubject()).willReturn("s1");
    given(cursorQuad1.getPredicate()).willReturn("p1");
    given(cursorQuad1.getChangeType()).willReturn(ChangeType.ASSERTED);
    given(cursorQuad1.getObject()).willReturn("o1");
    given(cursorQuad1.getDirection()).willReturn(Direction.OUT);

    CursorQuad cursorQuad2 = mock(CursorQuad.class);
    given(cursorQuad2.getSubject()).willReturn("s2");
    given(cursorQuad2.getPredicate()).willReturn("p2");
    given(cursorQuad2.getChangeType()).willReturn(ChangeType.RETRACTED);
    given(cursorQuad2.getObject()).willReturn("o2");
    given(cursorQuad2.getDirection()).willReturn(Direction.OUT);

    CursorQuad cursorQuad3 = mock(CursorQuad.class);
    given(cursorQuad3.getSubject()).willReturn("s3");
    given(cursorQuad3.getPredicate()).willReturn("p3");
    given(cursorQuad3.getChangeType()).willReturn(ChangeType.ASSERTED);
    given(cursorQuad3.getObject()).willReturn("o3");
    given(cursorQuad3.getDirection()).willReturn(Direction.OUT);
    given(cursorQuad3.getValuetype()).willReturn(Optional.of(RdfConstants.STRING));

    CursorQuad cursorQuad4 = mock(CursorQuad.class);
    given(cursorQuad4.getSubject()).willReturn("s4");
    given(cursorQuad4.getPredicate()).willReturn("p4");
    given(cursorQuad4.getChangeType()).willReturn(ChangeType.RETRACTED);
    given(cursorQuad4.getObject()).willReturn("o4");
    given(cursorQuad4.getDirection()).willReturn(Direction.OUT);
    given(cursorQuad4.getValuetype()).willReturn(Optional.of(RdfConstants.STRING));

    CursorQuad cursorQuad5 = mock(CursorQuad.class);
    given(cursorQuad5.getSubject()).willReturn("s5");
    given(cursorQuad5.getPredicate()).willReturn("p5");
    given(cursorQuad5.getChangeType()).willReturn(ChangeType.ASSERTED);
    given(cursorQuad5.getObject()).willReturn("o5");
    given(cursorQuad5.getDirection()).willReturn(Direction.OUT);
    given(cursorQuad5.getValuetype()).willReturn(Optional.of(RdfConstants.LANGSTRING));
    given(cursorQuad5.getLanguage()).willReturn(Optional.of("en"));

    CursorQuad cursorQuad6 = mock(CursorQuad.class);
    given(cursorQuad6.getSubject()).willReturn("s6");
    given(cursorQuad6.getPredicate()).willReturn("p6");
    given(cursorQuad6.getChangeType()).willReturn(ChangeType.RETRACTED);
    given(cursorQuad6.getObject()).willReturn("o6");
    given(cursorQuad6.getDirection()).willReturn(Direction.OUT);
    given(cursorQuad6.getValuetype()).willReturn(Optional.of(RdfConstants.LANGSTRING));
    given(cursorQuad6.getLanguage()).willReturn(Optional.of("en"));

    given(bdbTruePatchStore.getChangesOfVersion(1, true))
      .willReturn(Stream.of(cursorQuad1, cursorQuad2, cursorQuad3, cursorQuad4, cursorQuad5, cursorQuad6));

    ChangeListBuilder changeListBuilder = new ChangeListBuilder("graph");

    Integer version = 1;

    ChangesRetriever changesRetriever = new ChangesRetriever(bdbTruePatchStore, null);

    List<String> changes = changeListBuilder.retrieveChanges(changesRetriever, version).collect(Collectors.toList());

    assertThat(changes, contains(
      "+<s1> <p1> <o1> <graph> .\n",
      "-<s2> <p2> <o2> <graph> .\n",
      "+<s3> <p3> \"o3\"^^<http://www.w3.org/2001/XMLSchema#string> <graph> .\n",
      "-<s4> <p4> \"o4\"^^<http://www.w3.org/2001/XMLSchema#string> <graph> .\n",
      "+<s5> <p5> \"o5\"@en <graph> .\n",
      "-<s6> <p6> \"o6\"@en <graph> .\n"
    ));
  }
}
