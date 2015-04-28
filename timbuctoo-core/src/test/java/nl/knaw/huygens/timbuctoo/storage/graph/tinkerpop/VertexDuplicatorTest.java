package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeMockBuilder.anEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class VertexDuplicatorTest {
  private static final String VERSION_OF_EDGE_LABEL = SystemRelationType.VERSION_OF.name();
  private static final String OUTGOING_EDGE_LABEL = "outgoing";
  private static final String INCOMING_EDGE_LABEL = "incomming";
  private static final int REV = 1;
  private static final String ID = "id";
  private Graph dbMock;
  private Vertex vertexToDuplicate;
  private Vertex duplicate;
  private VertexDuplicator instance;
  private Vertex otherVertex;

  @Before
  public void setup() {
    otherVertex = aVertex().build();
    vertexToDuplicate = setupVertexToDuplicate();
    duplicate = aVertex().build();
    dbMock = mock(Graph.class);
    when(dbMock.addVertex(null)).thenReturn(duplicate);

    instance = new VertexDuplicator(dbMock);
  }

  private Vertex setupVertexToDuplicate() {
    Edge incomingEdge = anEdge().withLabel(INCOMING_EDGE_LABEL).withTarget(duplicate).withSource(otherVertex).build();
    Edge outgoingEdge = anEdge().withLabel(OUTGOING_EDGE_LABEL).withSource(duplicate).withTarget(otherVertex).build();
    Edge versionOfEdge = anEdge().withLabel(VERSION_OF_EDGE_LABEL).withSource(duplicate).withTarget(otherVertex).build();

    return aVertex() //
        .withId(ID) //
        .withRev(REV) //
        .withIncomingEdgeWithLabel(INCOMING_EDGE_LABEL, incomingEdge) //
        .withOutgoingEdgeWithLabel(VERSION_OF_EDGE_LABEL, versionOfEdge)//
        .withOutgoingEdgeWithLabel(OUTGOING_EDGE_LABEL, outgoingEdge)//
        .build();
  }

  @Test
  public void duplicateCopiesAllThePropertiesOfTheNode() {
    // action
    instance.duplicate(vertexToDuplicate);

    // verify
    verifyIdIsSet(duplicate);
    verifyRevIsSet(duplicate);

  }

  private void verifyRevIsSet(Vertex duplicate) {
    verify(duplicate).setProperty(Entity.REVISION_PROPERTY_NAME, REV);
  }

  private void verifyIdIsSet(Vertex duplicate) {
    verify(duplicate).setProperty(Entity.ID_PROPERTY_NAME, ID);
  }

  @Test
  public void duplicateCopiesAllTheEdgesOftheNodeExceptIsVersionOf() {
    // action
    instance.duplicate(vertexToDuplicate);

    // verify
    verify(duplicate).addEdge(OUTGOING_EDGE_LABEL, otherVertex);
    verify(otherVertex).addEdge(INCOMING_EDGE_LABEL, duplicate);
    verify(duplicate, times(0)).addEdge(VERSION_OF_EDGE_LABEL, otherVertex);
  }

  @Test
  public void duplicateCreatesAVersionOfEdgeBetweenTheDuplicateAndTheOriginal() {
    // action
    instance.duplicate(vertexToDuplicate);

    // verify
    verify(duplicate).addEdge(VERSION_OF_EDGE_LABEL, vertexToDuplicate);
  }
}
