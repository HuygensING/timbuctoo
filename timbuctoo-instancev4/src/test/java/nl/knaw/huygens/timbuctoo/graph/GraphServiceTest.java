package nl.knaw.huygens.timbuctoo.graph;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class GraphServiceTest {


  private static final String RELATION_NAME = "relationName";
  private static final String RELATION_NAME_2 = "relationName2";
  private static final String UNREQUESTED_RELATION = "unrequested";

  private static final String TYPES = "[\"person\", \"wwperson\"]";
  private static final String THE_UUID = "077bf0b5-6b7d-45aa-89ff-6ecf2cfc549c";


  private Edge mockEdge(String relationName) {
    Edge edgeMock = mock(Edge.class);
    given(edgeMock.label()).willReturn(relationName);
    return edgeMock;
  }


  private GraphWrapper createGraphWrapper(Graph graph) {
    return new GraphWrapper() {
      @Override
      public Graph getGraph() {
        return graph;
      }

      @Override
      public GraphTraversalSource getLatestState() {
        return graph.traversal();
      }

      @Override
      public GraphTraversal<Vertex, Vertex> getCurrentEntitiesFor(String... entityTypeNames) {
        return graph.traversal().V();
      }
    };
  }

  @Test
  public void getShouldGenerateAD3Graph() throws NotFoundException, IOException {

    Graph graph = newGraph()
            // Should be first index in the node list because it is the requested entity
            .withVertex("v1", v -> v
                    .withProperty("isLatest", true)
                    .withTimId(THE_UUID)
                    .withProperty("types", TYPES)
                    .withProperty("wwperson_tempName", "name1")
            )
            // Should come back as direct relation to v1 because RELATION_NAME is requested
            .withVertex("v2", v -> v
                    .withTimId("2")
                    .withProperty("types", TYPES)
                    .withOutgoingRelation(RELATION_NAME, "v1")
                    .withProperty("wwperson_tempName", "name2")
            )
            // Should come back as direct relation to v1 because RELATION_NAME_2 is requested
            .withVertex("v3", v -> v
                    .withTimId("3")
                    .withProperty("types", TYPES)
                    .withOutgoingRelation(RELATION_NAME_2, "v1")
                    .withProperty("wwperson_tempName", "name3")
            )
            // Should come back as once removed relation to v2->v1 because depth=2
            .withVertex("v4", v -> v
                    .withTimId("4")
                    .withProperty("types", TYPES)
                    .withOutgoingRelation(RELATION_NAME, "v2")
                    .withProperty("wwperson_tempName", "name4")
            )
            // Should not come back because UNREQUESTED_RELATION is not requested
            .withVertex("v5", v -> v
                    .withTimId("5")
                    .withProperty("types", TYPES)
                    .withOutgoingRelation(UNREQUESTED_RELATION, "v1")
                    .withProperty("wwperson_tempName", "name5")
            )
            // Should not come back because it is twice removed and depth=2
            .withVertex("v6", v -> v
                    .withTimId("6")
                    .withProperty("types", TYPES)
                    .withOutgoingRelation(RELATION_NAME, "v4")
                    .withProperty("wwperson_tempName", "name6")
            )
            .build();

    List<Vertex> vertices = graph.traversal().V().asAdmin().clone().toList();
    Collections.sort(vertices, (vertexA, vertexB) -> ((String) vertexA.property("wwperson_tempName").value())
            .compareTo((String) vertexB.property("wwperson_tempName").value()));

    GraphWrapper graphWrapper = createGraphWrapper(graph);
    GraphService underTest = new GraphService(graphWrapper);

    D3Graph result = underTest.get(
            "wwperson", UUID.fromString(THE_UUID), Lists.newArrayList(RELATION_NAME, RELATION_NAME_2), 2);

    assertThat(result.getNodes().get(0), is(new Node(vertices.get(0))));
    assertThat(result.getNodes(), containsInAnyOrder(
            new Node(vertices.get(0)),
            new Node(vertices.get(1)),
            new Node(vertices.get(2)),
            new Node(vertices.get(3))
    ));

    assertThat(result.getLinks(), containsInAnyOrder(
            new Link(mockEdge(RELATION_NAME), 0, 1),
            new Link(mockEdge(RELATION_NAME_2), 0, 2),
            new Link(mockEdge(RELATION_NAME), 1, 3)
    ));
  }
}
