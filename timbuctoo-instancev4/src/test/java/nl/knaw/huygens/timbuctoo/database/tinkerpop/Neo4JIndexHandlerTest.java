package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Neo4JIndexHandlerTest {

  public static final String COLLECTION = "collection";

  @Test
  public void hasIndexForReturnsTrueIfTheIndexExists() {
    TinkerpopGraphManager tinkerpopGraphManager = newGraph().wrap();
    Collection collection = mock(Collection.class);
    when(collection.getCollectionName()).thenReturn(COLLECTION);
    createIndexFor(tinkerpopGraphManager, collection);
    Neo4jIndexHandler instance = new Neo4jIndexHandler(tinkerpopGraphManager);

    boolean existingIndex = instance.hasIndexFor(collection);

    assertThat(existingIndex, is(true));
  }

  private void createIndexFor(TinkerpopGraphManager tinkerpopGraphManager, Collection collection) {
    Transaction tx = tinkerpopGraphManager.getGraphDatabase().beginTx();
    tinkerpopGraphManager.getGraphDatabase().index().forNodes(collection.getCollectionName());
    tx.close();
  }

  @Test
  public void hasIndexForReturnsFalseIfTheIndexDoesNotExist() {
    TinkerpopGraphManager tinkerpopGraphManager = newGraph().wrap();
    Neo4jIndexHandler instance = new Neo4jIndexHandler(tinkerpopGraphManager);

    Collection collectionWithoutIndex = mock(Collection.class);
    when(collectionWithoutIndex.getCollectionName()).thenReturn(COLLECTION);

    boolean existingIndex = instance.hasIndexFor(collectionWithoutIndex);

    assertThat(existingIndex, is(false));
  }

  @Test
  public void findByDisplayNameRetrievesTheVerticesFromTheIndexAndCreatesTraversalForThem() {
    String id1 = UUID.randomUUID().toString();
    String id2 = UUID.randomUUID().toString();
    TinkerpopGraphManager tinkerpopGraphManager = newGraph()
      .withVertex(v -> v
        .withTimId(id1)
        .withProperty("displayName", "query"))
      .withVertex(v -> v
        .withTimId(id2)
        .withProperty("displayName", "query2"))
      .withVertex(v -> v
        .withTimId(UUID.randomUUID())
        .withProperty("displayName", "notmatching")
      )
      .wrap();
    Neo4jIndexHandler instance = new Neo4jIndexHandler(tinkerpopGraphManager);
    Collection collection = mock(Collection.class);
    when(collection.getCollectionName()).thenReturn(COLLECTION);
    addToIndex(instance, collection, tinkerpopGraphManager.getGraph().traversal().V().has("tim_id", id1).next());
    addToIndex(instance, collection, tinkerpopGraphManager.getGraph().traversal().V().has("tim_id", id2).next());

    GraphTraversal<Vertex, Vertex> vertices = instance.findByQuickSearch(collection, "query*");

    assertThat(vertices.map(v -> v.get().value("tim_id")).toList(), containsInAnyOrder(id1, id2));
  }

  private void addToIndex(Neo4jIndexHandler instance, Collection collection, Vertex vertex) {
    instance.addToQuickSearchIndex(collection, vertex.value("displayName"), vertex);
  }

  @Test
  public void findKeywordsByDisplayNameFiltersTheIndexResultsOnTheRightKeywordType() {
    String id1 = UUID.randomUUID().toString();
    String id2 = UUID.randomUUID().toString();
    TinkerpopGraphManager tinkerpopGraphManager = newGraph()
      .withVertex(v -> v
        .withTimId(id1)
        .withProperty("keyword_type", "keywordType")
        .withProperty("displayName", "query"))
      .withVertex(v -> v
        .withTimId(id2)
        .withProperty("displayName", "query2"))
      .withVertex(v -> v
        .withTimId(UUID.randomUUID())
        .withProperty("displayName", "notmatching")
      )
      .wrap();
    Neo4jIndexHandler instance = new Neo4jIndexHandler(tinkerpopGraphManager);
    Collection collection = mock(Collection.class);
    when(collection.getCollectionName()).thenReturn(COLLECTION);
    addToIndex(instance, collection, tinkerpopGraphManager.getGraph().traversal().V().has("tim_id", id1).next());
    addToIndex(instance, collection, tinkerpopGraphManager.getGraph().traversal().V().has("tim_id", id2).next());

    GraphTraversal<Vertex, Vertex> vertices = instance.findKeywordsByQuickSearch(collection, "query", "keywordType");

    assertThat(vertices.map(v -> v.get().value("tim_id")).toList(), containsInAnyOrder(id1));
  }
}
