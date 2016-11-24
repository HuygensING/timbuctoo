package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Neo4JIndexHandlerTest {

  public static final String COLLECTION = "collection";

  @Test
  public void hasIndexForRedirectsToTheNeo4jGraph() {
    IndexManager indexManager = mock(IndexManager.class);
    Neo4jIndexHandler instance = new Neo4jIndexHandler(indexManager, newGraph().wrap());
    Collection collection = mock(Collection.class);
    when(collection.getCollectionName()).thenReturn(COLLECTION);

    instance.hasIndexFor(collection);

    verify(indexManager).existsForNodes(COLLECTION);
  }

  @Test
  public void getVerticesRetrievesTheVerticesFromTheIndexAndCreatesTraversalForThem() {
    String id1 = UUID.randomUUID().toString();
    String id2 = UUID.randomUUID().toString();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v.withTimId(id1))
      .withVertex(v -> v.withTimId(id2))
      .withVertex(v -> v.withTimId(UUID.randomUUID()))
      .wrap();
    Node node1 = createNodeWithId(graphWrapper.getGraph().traversal().V().has("tim_id", id1).id().next());
    Node node2 = createNodeWithId(graphWrapper.getGraph().traversal().V().has("tim_id", id2).id().next());
    IndexManager indexManager = createIndexManager(node1, node2);
    Neo4jIndexHandler instance = new Neo4jIndexHandler(indexManager, graphWrapper);
    Collection collection = mock(Collection.class);
    when(collection.getCollectionName()).thenReturn(COLLECTION);

    GraphTraversal<Vertex, Vertex> vertices = instance.getVerticesByDisplayName(collection, "query");

    assertThat(vertices.map(v -> v.get().value("tim_id")).toList(), containsInAnyOrder(id1, id2));
  }

  @Test
  public void getKeywordVerticesFiltersTheIndexResultsOnTheRightKeywordType() {
    String id1 = UUID.randomUUID().toString();
    String id2 = UUID.randomUUID().toString();
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withTimId(id1)
        .withProperty("keyword_type", "keywordType")
      )
      .withVertex(v -> v.withTimId(id2))
      .withVertex(v -> v.withTimId(UUID.randomUUID()))
      .wrap();
    Node node1 = createNodeWithId(graphWrapper.getGraph().traversal().V().has("tim_id", id1).id().next());
    Node node2 = createNodeWithId(graphWrapper.getGraph().traversal().V().has("tim_id", id2).id().next());
    IndexManager indexManager = createIndexManager(node1, node2);
    Neo4jIndexHandler instance = new Neo4jIndexHandler(indexManager, graphWrapper);
    Collection collection = mock(Collection.class);
    when(collection.getCollectionName()).thenReturn(COLLECTION);

    GraphTraversal<Vertex, Vertex> vertices = instance.getKeywordVertices(collection, "query", "keywordType");

    assertThat(vertices.map(v -> v.get().value("tim_id")).toList(), containsInAnyOrder(id1));
  }

  private IndexManager createIndexManager(Node node1, Node node2) {
    IndexManager indexManager = mock(IndexManager.class);
    Index index = mock(Index.class);
    String query = "query";
    IndexHits<Node> results = mock(IndexHits.class);
    when(results.spliterator()).thenReturn(Lists.newArrayList(node1, node2).spliterator());
    when(index.query("displayName", query)).thenReturn(results);
    when(indexManager.forNodes(COLLECTION)).thenReturn(index);
    return indexManager;
  }

  private Node createNodeWithId(Object id) {
    Node node1 = mock(Node.class);
    when(node1.getId()).thenReturn((long) id);
    return node1;
  }

}
