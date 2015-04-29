package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeMockBuilder.anEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class EdgeDuplicatorTest {
  private static final String PID = "pid";
  private static final int REV = 2;
  private static final String LABEL = "label";
  private Vertex sourceVertex;
  private Vertex targetVertex;
  private Edge edge;
  private EdgeDuplicator instance;
  private Edge duplicate;

  @Before
  public void setup() {
    sourceVertex = aVertex().build();
    targetVertex = aVertex().build();
    setupEdge();
    instance = new EdgeDuplicator();

    duplicate = anEdge().build();
    when(sourceVertex.addEdge(LABEL, targetVertex)).thenReturn(duplicate);
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
    verify(duplicate).setProperty(REVISION_PROPERTY_NAME, REV);
    verify(duplicate).setProperty(DomainEntity.PID, PID);
  }
}
