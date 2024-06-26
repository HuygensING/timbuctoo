package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.QuadGraphs;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Lists.newArrayList;
import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.ChangeType.ASSERTED;
import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.ChangeType.UNCHANGED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IterationStateTest {
  @Test
  public void prefersAssertionOverUnchanged() throws Exception {
    final IterationState iterationState = new IterationState(
      QuadGraphs.mapToQuadGraphs(newArrayList(
        CursorQuad.create("subj", "pred", Direction.OUT, ASSERTED, "obj", null, null, null, "")
      ).stream()),
      QuadGraphs.mapToQuadGraphs(newArrayList(
        CursorQuad.create("subj", "pred", Direction.OUT, UNCHANGED, "obj", null, null, null, "")
      ).stream()),
      true
    );

    assertThat(iterationState.next().getChangeType(), is(ASSERTED));
  }

  @Test
  public void ignoresAssertionsIfUnchanged() throws Exception {
    final IterationState iterationState = new IterationState(
      QuadGraphs.mapToQuadGraphs(newArrayList(
        CursorQuad.create("subj", "pred", Direction.OUT, ASSERTED, "obj", null, null, null, "")
      ).stream()),
        QuadGraphs.mapToQuadGraphs(newArrayList(
        CursorQuad.create("subj", "pred", Direction.OUT, UNCHANGED, "obj", null, null, null, "")
      ).stream()),
      false
    );

    assertThat(iterationState.hasNext(), is(false));
  }

  @Test
  public void ignoresAssertionsIfUnchangedInTheMiddle() throws Exception {
    final IterationState iterationState = new IterationState(
      QuadGraphs.mapToQuadGraphs(newArrayList(
        CursorQuad.create("subj", "pred1", Direction.OUT, ASSERTED, "obj", null, null, null, "")
      ).stream()),
      QuadGraphs.mapToQuadGraphs(newArrayList(
        CursorQuad.create("subj", "pred0", Direction.OUT, UNCHANGED, "obj", null, null, null, ""),
        CursorQuad.create("subj", "pred1", Direction.OUT, UNCHANGED, "obj", null, null, null, ""),
        CursorQuad.create("subj", "pred2", Direction.OUT, UNCHANGED, "obj", null, null, null, "")
      ).stream()),
      false
    );

    assertThat(newArrayList(iterationState).size(), is(2));
  }
}
