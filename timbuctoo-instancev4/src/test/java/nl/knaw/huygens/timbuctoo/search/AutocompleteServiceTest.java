package nl.knaw.huygens.timbuctoo.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain.SingleEntity;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.search.description.indexes.MockIndexUtil.makeIndexMocks;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AutocompleteServiceTest {


  private AutocompleteService instance;
  private String collectionName;
  private IndexManager mockIndexManager;
  private Index mockIndex;
  private Vertex mockVertex1;
  private Vertex mockVertex2;
  private String timId1;
  private String timId2;
  private String keyword1;
  private String keyword2;


  private Vertex getMockVertex(String timId, String propertyValue, String propertyName, int rev) {
    VertexProperty<Object> mockVertexTimIdProp = mock(VertexProperty.class);
    VertexProperty<Object> mockVertexRevProp = mock(VertexProperty.class);
    Vertex mockVertex = mock(Vertex.class);
    given(mockVertexTimIdProp.value()).willReturn(timId);
    given(mockVertexRevProp.value()).willReturn(rev);
    given(mockVertex.property("tim_id")).willReturn(mockVertexTimIdProp);
    given(mockVertex.property("rev")).willReturn(mockVertexRevProp);
    given(mockVertex.value(propertyName)).willReturn(propertyValue);
    given(mockVertex.keys()).willReturn(Sets.newHashSet(propertyName));
    return mockVertex;
  }


  @Before
  public void setUp() {
    final List<Object> mocks = makeIndexMocks();

    this.collectionName = "wwkeywords";
    timId1 = UUID.randomUUID().toString();
    timId2 = UUID.randomUUID().toString();
    keyword1 = "test keyword";
    keyword2 = "test keyword 2";
    mockIndex = (Index) mocks.get(1);
    mockIndexManager = (IndexManager) mocks.get(4);
    mockVertex1 = getMockVertex(timId1, keyword1, "wwkeyword_value", 2);
    mockVertex2 = getMockVertex(timId2, keyword2, "wwkeyword_value", 1);


    final UrlGenerator urlGenerator = (coll, id, rev) -> URI.create(SingleEntity.makeUrl(coll, id, rev).getPath());
    final GraphDatabaseService mockDatabaseService = (GraphDatabaseService) mocks.get(0);
    final Graph mockGraph = mock(Graph.class);
    final GraphTraversalSource mockTraversal = mock(GraphTraversalSource.class);
    final Transaction mockTransaction = mock(Transaction.class);
    final TinkerpopGraphManager mockGraphManager = mock(TinkerpopGraphManager.class);
    final GraphTraversal<Vertex, Vertex> mockVertexTraversal1 = mock(GraphTraversal.class);
    final GraphTraversal<Vertex, Vertex> mockVertexTraversal2 = mock(GraphTraversal.class);
    final IndexHits<Node> mockResults = mock(IndexHits.class);
    final Node resultNode = mock(Node.class);
    final Node resultNode2 = mock(Node.class);
    final List<Node> mockResultList = Lists.newArrayList(resultNode, resultNode2);

    given(mockGraphManager.getGraphDatabase()).willReturn(mockDatabaseService);
    given(mockGraphManager.getGraph()).willReturn(mockGraph);
    given(mockGraph.tx()).willReturn(mockTransaction);
    given(mockGraph.traversal()).willReturn(mockTraversal);
    given(mockIndexManager.existsForNodes(anyString())).willReturn(true);
    given(mockResults.spliterator()).willReturn(mockResultList.spliterator());
    given(mockIndex.query(anyString(), anyString())).willReturn(mockResults);
    given(resultNode.getId()).willReturn(1L);
    given(resultNode2.getId()).willReturn(2L);
    given(mockVertexTraversal1.next()).willReturn(mockVertex1);
    given(mockVertexTraversal2.next()).willReturn(mockVertex2);
    given(mockTraversal.V(1L)).willReturn(mockVertexTraversal1);
    given(mockTraversal.V(2L)).willReturn(mockVertexTraversal2);
    given(resultNode.getProperty("keyword_type")).willReturn("maritalStatus");
    given(resultNode2.getProperty("keyword_type")).willReturn("somethingElse");

    this.instance = new AutocompleteService(mockGraphManager, urlGenerator);
  }

  @Test
  public void searchInvokesLuceneIndexAndReturnsResultsAsJsonNode() throws InvalidCollectionException {
    final JsonNode results = instance.search(collectionName, Optional.of("*foo bar*"), Optional.empty());

    verify(mockIndexManager, times(1)).existsForNodes(collectionName);
    verify(mockIndex, times(1)).query("displayName", "foo AND bar*");
    verify(mockVertex1, times(1)).property("tim_id");
    verify(mockVertex1, times(1)).property("rev");
    verify(mockVertex2, times(1)).property("tim_id");
    verify(mockVertex2, times(1)).property("rev");
    assertThat(results.get(0).get("key").asText(), equalTo("/v2.1/domain/wwkeywords/" + timId1));
    assertThat(results.get(1).get("key").asText(), equalTo("/v2.1/domain/wwkeywords/" + timId2));
    assertThat(results.get(0).get("value").asText(), equalTo(keyword1));
    assertThat(results.get(1).get("value").asText(), equalTo(keyword2));

  }

  @Test
  public void searchFiltersKeywordsByType() throws InvalidCollectionException {
    final JsonNode results = instance.search(collectionName, Optional.of("*foo bar*"), Optional.of("maritalStatus"));

    assertThat(results.size(), equalTo(1));
  }


  @Test(expected = InvalidCollectionException.class)
  public void searchThrowsWhenNoIndexIsFoundForTheCollectionName() throws InvalidCollectionException {
    List<Object> mocks = makeIndexMocks();
    GraphDatabaseService mockDatabaseService = (GraphDatabaseService) mocks.get(0);
    Graph mockGraph = mock(Graph.class);
    Transaction mockTransaction = mock(Transaction.class);
    TinkerpopGraphManager mockGraphManager = mock(TinkerpopGraphManager.class);

    given(mockGraphManager.getGraphDatabase()).willReturn(mockDatabaseService);
    given(mockGraphManager.getGraph()).willReturn(mockGraph);
    given(mockGraph.tx()).willReturn(mockTransaction);


    AutocompleteService underTest = new AutocompleteService(mockGraphManager, null);
    underTest.search("nonexistent", Optional.empty(), Optional.empty());
  }



}
