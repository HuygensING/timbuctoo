package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.search.description.indexes.MockIndexUtil.makeIndexMocks;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class Neo4jLuceneEntityFetcherTest {

  @Test
  public void getEntityLooksInLuceneIndexIfAvailable() {
    final UUID id = UUID.randomUUID();
    final String collectionName = "things";
    final List<Object> indexMocks = makeIndexMocks();
    final long nativeId = 123L;
    final long secondId = 321L;

    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) indexMocks.get(0);
    Index mockIndex = (Index) indexMocks.get(1);
    IndexManager mockIndexManager = (IndexManager) indexMocks.get(4);

    TinkerpopGraphManager graphManager = mock(TinkerpopGraphManager.class);
    IndexHits indexHits = mock(IndexHits.class);
    Node node = mock(Node.class);

    Graph graph = newGraph().build();
    GraphTraversalSource traversal = mock(GraphTraversalSource.class);
    Iterator verticesIt = mock(Iterator.class);
    GraphTraversal vertexT = mock(GraphTraversal.class);

    given(graphManager.getGraphDatabase()).willReturn(mockDatabaseService);
    given(graphManager.getGraph()).willReturn(graph);
    given(mockIndex.get("tim_id", id.toString())).willReturn(indexHits);
    given(mockIndexManager.existsForNodes(collectionName)).willReturn(true);
    given(indexHits.size()).willReturn(1);
    given(node.getId()).willReturn(nativeId);

    given(traversal.V(nativeId)).willReturn(vertexT);
    GraphTraversal<Vertex, Vertex> vertexT2 = mock(GraphTraversal.class);

    given(traversal.V(secondId)).willReturn(vertexT2);

    Vertex foundVertex = mock(Vertex.class);
    given(vertexT.hasNext()).willReturn(true);
    given(vertexT.next()).willReturn(foundVertex);
    given(foundVertex.value("isLatest")).willReturn(true);
    given(foundVertex.vertices(any(), anyString())).willReturn(verticesIt);
    given(indexHits.next()).willReturn(node);

    given(foundVertex.id()).willReturn(secondId);

    Neo4jLuceneEntityFetcher instance = new Neo4jLuceneEntityFetcher(graphManager);

    GraphTraversal<Vertex, Vertex> entityT = instance.getEntity(traversal, id, null, collectionName);

    verify(traversal, times(1)).V(nativeId);
    verify(traversal, times(1)).V(secondId);

    assertThat(entityT, equalTo(vertexT2));

  }

  @Test
  public void getEntityInvokesSuperIfLuceneIndexIsNotAvailable() {
    final UUID id = UUID.randomUUID();
    final String things = "things";
    final List<Object> indexMocks = makeIndexMocks();

    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) indexMocks.get(0);
    IndexManager mockIndexManager = (IndexManager) indexMocks.get(4);
    TinkerpopGraphManager graphManager = mock(TinkerpopGraphManager.class);

    given(mockIndexManager.existsForNodes("things")).willReturn(false);

    given(graphManager.getGraphDatabase()).willReturn(mockDatabaseService);

    Graph graph = newGraph()
      .withVertex(v -> v
        .withProperty("tim_id", id.toString())
        .withProperty("isLatest", true)
        .withProperty("deleted", false)
      ).build();

    GraphTraversalSource traversal = graph.traversal();

    given(graphManager.getGraph()).willReturn(graph);

    Neo4jLuceneEntityFetcher instance = new Neo4jLuceneEntityFetcher(graphManager);

    GraphTraversal<Vertex, Vertex> entityT = instance.getEntity(traversal, id, null, things);

    assertThat(entityT.next().value("tim_id"), equalTo(id.toString()));
  }

  @Test
  public void getEntityInvokesSuperIfNodeIsNotFoundInIndex() {
    final UUID id = UUID.randomUUID();
    final String things = "things";
    final List<Object> indexMocks = makeIndexMocks();

    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) indexMocks.get(0);
    Index mockIndex = (Index) indexMocks.get(1);

    IndexManager mockIndexManager = (IndexManager) indexMocks.get(4);
    TinkerpopGraphManager graphManager = mock(TinkerpopGraphManager.class);

    given(mockIndexManager.existsForNodes("things")).willReturn(true);
    IndexHits indexHits = mock(IndexHits.class);

    given(graphManager.getGraphDatabase()).willReturn(mockDatabaseService);
    given(mockIndex.get("tim_id", id.toString())).willReturn(indexHits);
    given(indexHits.size()).willReturn(0);

    Graph graph = newGraph()
      .withVertex(v -> v
        .withProperty("tim_id", id.toString())
        .withProperty("isLatest", true)
        .withProperty("deleted", false)
      ).build();

    GraphTraversalSource traversal = graph.traversal();

    given(graphManager.getGraph()).willReturn(graph);

    Neo4jLuceneEntityFetcher instance = new Neo4jLuceneEntityFetcher(graphManager);

    GraphTraversal<Vertex, Vertex> entityT = instance.getEntity(traversal, id, null, things);

    assertThat(entityT.next().value("tim_id"), equalTo(id.toString()));
  }
}
