package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;

public class VertexFinderTest {
  private static final String ID = "id";
  private Graph graph;
  private VertexFinder instance;
  private GraphQuery query;

  @Before
  public void setup() {
    setupGraph();
    instance = new VertexFinder(graph);
  }

  private void setupGraph() {
    graph = mock(Graph.class);
    query = mock(GraphQuery.class);
    when(graph.query()).thenReturn(query);
  }

  @Test
  public void getLatestByIdRetrievesTheLatestFromAGraphQuery() {
    // setup 
    Vertex latestVertex = createLatestVertex();
    Vertex otherVertex1 = createNonLatestVertex();
    Vertex otherVertex2 = createNonLatestVertex();

    when(query.has(Entity.ID_DB_PROPERTY_NAME, ID)).thenReturn(query);
    when(query.vertices()).thenReturn(Lists.newArrayList(otherVertex1, latestVertex, otherVertex2));

    // action
    Vertex actualVertex = instance.getLatestVertexById(ID);

    // verify
    assertThat(actualVertex, is(sameInstance(latestVertex)));
  }

  private Vertex createLatestVertex() {
    return mock(Vertex.class);
  }

  private Vertex createNonLatestVertex() {
    Vertex otherVertex1 = createLatestVertex();
    when(otherVertex1.getEdges(Direction.OUT, SystemRelationType.VERSION_OF.name())).thenReturn(Lists.newArrayList(mock(Edge.class)));
    return otherVertex1;
  }
}
