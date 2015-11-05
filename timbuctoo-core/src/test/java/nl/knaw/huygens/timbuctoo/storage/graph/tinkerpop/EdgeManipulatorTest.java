package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.model.DomainEntity.DB_PID_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.model.Entity.DB_REV_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeMockBuilder.anEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EdgeManipulatorTest {
  private static final String PID = "pid";
  private static final int REV = 2;
  private static final String LABEL = "label";
  private Vertex sourceVertex;
  private Vertex targetVertex;
  private Edge edge;
  private EdgeManipulator instance;
  private Edge duplicate;
  private Vertex newSource;
  private Edge edgeWithNewSource;
  private Vertex newTarget;
  private Edge edgeWithNewTarget;

  @Before
  public void setup() {
    targetVertex = aVertex().build();
    newTarget = aVertex().build();

    setupSource();
    setupNewSource();

    setupEdge();

    instance = new EdgeManipulator();

    duplicate = anEdge().build();
    when(sourceVertex.addEdge(LABEL, targetVertex)).thenReturn(duplicate);
  }

  private void setupSource() {
    sourceVertex = aVertex().build();
    edgeWithNewTarget = anEdge().build();
    when(sourceVertex.addEdge(LABEL, newTarget)).thenReturn(edgeWithNewTarget);
  }

  private void setupNewSource() {
    newSource = aVertex().build();
    edgeWithNewSource = anEdge().build();
    when(newSource.addEdge(LABEL, targetVertex)).thenReturn(edgeWithNewSource);
  }

  private void setupEdge() {
    edge = anEdge() //
      .withLabel(LABEL)//
      .withSource(sourceVertex)//
      .withTarget(targetVertex)//
      .withRev(REV) //
      .withPID(PID)//
      .build();
  }

  @Test
  public void duplicateLetsTheStartNodeCreateANewEdgeWithTheSameEndNodeAndSameType() {
    // action
    instance.duplicate(edge);

    // verify
    verify(sourceVertex).addEdge(LABEL, targetVertex);
  }

  @Test
  public void duplicateCopiesAllThePropertiesOfTheOriginalEdgeToTheDuplicate() {
    // action
    instance.duplicate(edge);

    // verify
    verify(duplicate).setProperty(DB_REV_PROP_NAME, REV);
    verify(duplicate).setProperty(DB_PID_PROP_NAME, PID);
  }

  @Test
  public void duplicateAddsIsLatestPropertyAndSetsItToFalseOnTheEdgeToDuplicate() {
    // action
    instance.duplicate(edge);

    // verify
    verify(edge).setProperty(ElementFields.IS_LATEST, false);
    verify(duplicate).setProperty(ElementFields.IS_LATEST, true);
  }

  @Test
  public void changeSourceCreatesANewEdgeBetweenTheNewSourceAndTheEdgesTarget() {
    // action
    instance.changeSource(edge, newSource);

    // verify
    verify(newSource).addEdge(LABEL, targetVertex);
  }

  @Test
  public void changeSourceCopiesThePropertiesOfTheEdgeToTheNewlyCreated() {
    // action
    instance.changeSource(edge, newSource);

    // verify
    verify(edgeWithNewSource).setProperty(DB_REV_PROP_NAME, REV);
    verify(edgeWithNewSource).setProperty(DB_PID_PROP_NAME, PID);
  }

  @Test
  public void changeSourceRemovesTheOriginalEdge() {
    // action
    instance.changeSource(edge, newSource);

    // verify
    verify(edge).remove();
  }

  @Test
  public void changeTargetCreatesANewEdgeBetweenTheEdgesSourceAndTheNewTarget() {
    // action
    instance.changeTarget(edge, newTarget);

    // verify
    verify(sourceVertex).addEdge(LABEL, newTarget);
  }

  @Test
  public void changeTargetCopiesThePropertiesOfTheEdgeToTheNewlyCreated() {
    // action
    instance.changeTarget(edge, newTarget);

    // verify
    verify(edgeWithNewTarget).setProperty(DB_REV_PROP_NAME, REV);
    verify(edgeWithNewTarget).setProperty(DB_PID_PROP_NAME, PID);
  }

  @Test
  public void changeTargetRemovesTheOriginalEdge() {
    // action
    instance.changeTarget(edge, newTarget);

    // verify
    verify(edge).remove();
  }

}
