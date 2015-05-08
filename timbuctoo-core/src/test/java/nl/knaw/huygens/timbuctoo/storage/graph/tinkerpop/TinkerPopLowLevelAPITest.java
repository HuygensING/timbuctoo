package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType.VERSION_OF;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeMockBuilder.anEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeSearchResultBuilder.anEdgeSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeSearchResultBuilder.anEmptyEdgeSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.TinkerPopQueryMockBuilder.aQuery;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexSearchResultBuilder.aVertexSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexSearchResultBuilder.anEmptyVertexSearchResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import test.model.TestSystemEntityWrapper;
import test.model.projecta.SubADomainEntity;
import test.model.projecta.SubARelation;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;

public class TinkerPopLowLevelAPITest {
  private static final String PROPERTY_VALUE = "propertyValue";
  private static final String PROPERTY_NAME = "propertyName";
  private static final String ID2 = "id2";
  private static final int SECOND_REVISION = 2;
  private static final int THIRD_REVISION = 3;
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final int FIRST_REVISION = 1;
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final String ID = "id";
  private Graph dbMock;
  private TinkerPopLowLevelAPI instance;
  private VertexDuplicator vertexDuplicator;
  private EdgeDuplicator edgeDuplicator;

  @Before
  public void setup() {
    edgeDuplicator = mock(EdgeDuplicator.class);
    vertexDuplicator = mock(VertexDuplicator.class);
    dbMock = mock(Graph.class);
    instance = new TinkerPopLowLevelAPI(dbMock, vertexDuplicator, edgeDuplicator);
  }

  /* ************************************************************
   * Vertex
   * ************************************************************/

  @Test
  public void duplicateVertexDelegatesToVertexDuplicator() {
    // setup
    Vertex vertexToDuplicate = aVertex().build();

    // action
    instance.duplicate(vertexToDuplicate);

    // verify
    verify(vertexDuplicator).duplicate(vertexToDuplicate);
  }

  @Test
  public void findVerticesByPropertyReturnsAnIteratorWithTheLatestFoundVertices() {
    Vertex latestVertex1 = aVertex().build();
    Vertex latestVertex2 = aVertex().build();
    Vertex notLatestVertex = aVertex().withIncomingEdgeWithLabel(VERSION_OF).build();
    aVertexSearchResult() //
        .forType(DOMAIN_ENTITY_TYPE) //
        .forProperty(PROPERTY_NAME, PROPERTY_VALUE) //
        .containsVertex(latestVertex1) //
        .containsVertex(notLatestVertex) //
        .containsVertex(latestVertex2) //
        .foundInDatabase(dbMock);

    // action
    Iterator<Vertex> vertices = instance.findVerticesByProperty(DOMAIN_ENTITY_TYPE, PROPERTY_NAME, PROPERTY_VALUE);

    // verify
    assertThat(Lists.newArrayList(vertices), containsInAnyOrder(latestVertex1, latestVertex2));
  }

  @Test
  public void findVerticesByPropertyReturnsAnEmptuIteratorWhenNoVerticesAreFound() {
    anEmptyVertexSearchResult()//
        .forType(DOMAIN_ENTITY_TYPE)//
        .forProperty(PROPERTY_NAME, PROPERTY_VALUE)//
        .foundInDatabase(dbMock);

    // action
    Iterator<Vertex> vertices = instance.findVerticesByProperty(DOMAIN_ENTITY_TYPE, PROPERTY_NAME, PROPERTY_VALUE);

    // verify
    assertThat(Iterators.size(vertices), is(0));
  }

  @Test
  public void findVerticesWithoutPropertyReturnsVerticesDoNotContainACertainProperty() {
    // setup
    Vertex vertex1 = aVertex().build();
    Vertex vertex2 = aVertex().build();
    aVertexSearchResult()//
        .forType(DOMAIN_ENTITY_TYPE).withoutProperty(PROPERTY_NAME)//
        .containsVertex(vertex1) //
        .containsVertex(vertex2) //
        .foundInDatabase(dbMock);

    // action
    Iterator<Vertex> vertices = instance.findVerticesWithoutProperty(DOMAIN_ENTITY_TYPE, PROPERTY_NAME);

    // verify
    assertThat(Lists.newArrayList(vertices), containsInAnyOrder(vertex1, vertex2));
  }

  @Test
  public void findVerticesWithoutPropertyReturnsAnEmptyIteratorWhenNoneAreFound() {
    // setup
    anEmptyVertexSearchResult().forType(DOMAIN_ENTITY_TYPE).withoutProperty(PROPERTY_NAME).foundInDatabase(dbMock);

    // action
    Iterator<Vertex> vertices = instance.findVerticesWithoutProperty(DOMAIN_ENTITY_TYPE, PROPERTY_NAME);

    // verify
    assertThat(Iterators.size(vertices), is(0));
  }

  @Test
  public void getLatestVertexByIdReturnsTheVertexWithoutIncomingIsVersionOfRelation() {
    Vertex latestVertex = aVertex().build();
    aVertexSearchResult().forType(SYSTEM_ENTITY_TYPE).forId(ID) //
        .containsVertex(aVertex().withIncomingEdgeWithLabel(VERSION_OF).build()) //
        .andVertex(latestVertex) //
        .andVertex(aVertex().withIncomingEdgeWithLabel(VERSION_OF).build())//
        .foundInDatabase(dbMock);

    // action
    Vertex foundVertex = instance.getLatestVertexById(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(foundVertex, is(sameInstance(latestVertex)));
  }

  @Test
  public void getLatestVertexByIdReturnsNullIfNoVerticesAreFound() {
    // setup
    anEmptyVertexSearchResult().forType(SYSTEM_ENTITY_TYPE).forId(ID).foundInDatabase(dbMock);

    // action
    Vertex foundVertex = instance.getLatestVertexById(SYSTEM_ENTITY_TYPE, ID);

    // verify
    assertThat(foundVertex, is(nullValue()));
  }

  @Test
  public void getLatestVertexByIdReturnsTheLatestVertexWithACertainId() {
    Vertex latestVertex = aVertex().build();
    aVertexSearchResult().forId(ID) //
        .containsVertex(aVertex().withIncomingEdgeWithLabel(VERSION_OF).build()) //
        .andVertex(latestVertex) //
        .andVertex(aVertex().withIncomingEdgeWithLabel(VERSION_OF).build())//
        .foundInDatabase(dbMock);

    // action
    Vertex foundVertex = instance.getLatestVertexById(ID);

    // verify
    assertThat(foundVertex, is(sameInstance(latestVertex)));
  }

  @Test
  public void getLatestVertexByIdReturnsNullIfNoVerticesAreFoundWithACertainId() {
    anEmptyVertexSearchResult().forId(ID).foundInDatabase(dbMock);

    Vertex foundVertex = instance.getLatestVertexById(ID);

    // verify
    assertThat(foundVertex, is(nullValue()));
  }

  @Test
  public void getLatestVerticesOfReturnsOnlyTheLatestVersions() {
    // setup
    Vertex latestVertex1 = aVertex().build();
    Vertex latestVertex2 = aVertex().build();
    aVertexSearchResult().forType(SYSTEM_ENTITY_TYPE) //
        .containsVertex(latestVertex1) //
        .andVertex(aVertex().withIncomingEdgeWithLabel(VERSION_OF).build()) //
        .andVertex(aVertex().withIncomingEdgeWithLabel(VERSION_OF).build()) //
        .andVertex(latestVertex2) //
        .foundInDatabase(dbMock);

    // action
    Iterator<Vertex> foundVertices = instance.getLatestVerticesOf(SYSTEM_ENTITY_TYPE);

    // verify
    assertThat(foundVertices, is(notNullValue()));
    assertThat(Lists.newArrayList(foundVertices), contains(latestVertex1, latestVertex2));
  }

  @Test
  public void getLatestVerticesOfReturnsAnEmptyIteratorWhenNoVerticesAreFound() {
    // setup
    anEmptyVertexSearchResult().forType(SYSTEM_ENTITY_TYPE).foundInDatabase(dbMock);

    // action
    Iterator<Vertex> foundVertices = instance.getLatestVerticesOf(SYSTEM_ENTITY_TYPE);

    // verify
    assertThat(foundVertices, is(notNullValue()));
    assertThat(Iterators.size(foundVertices), is(0));
  }

  @Test
  public void getVertexWithRevisionReturnsTheVertexWithTheRevision() {
    // setup
    Vertex foundVertex = aVertex().build();
    aVertexSearchResult().forType(DOMAIN_ENTITY_TYPE).forId(ID).forRevision(FIRST_REVISION)//
        .containsVertex(foundVertex) //
        .foundInDatabase(dbMock);

    // action
    Vertex actualVertex = instance.getVertexWithRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(actualVertex, is(sameInstance(foundVertex)));
  }

  @Test
  public void getVertexWithRevisionReturnsNullIfNoVerticesAreFound() {
    // setup
    anEmptyVertexSearchResult().forType(DOMAIN_ENTITY_TYPE).forId(ID).forRevision(FIRST_REVISION).foundInDatabase(dbMock);

    // action
    Vertex vertex = instance.getVertexWithRevision(DOMAIN_ENTITY_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(vertex, is(nullValue()));
  }

  @Test
  public void getVerticesWithIdReturnsAnIteratorWithAllTheVerticesWithTheId() {
    // setup
    Vertex vertex1 = aVertex().build();
    Vertex vertex2 = aVertex().build();
    aVertexSearchResult() //
        .forType(DOMAIN_ENTITY_TYPE) //
        .forId(ID) //
        .containsVertex(vertex1) //
        .andVertex(vertex2) //
        .foundInDatabase(dbMock);

    // action
    Iterator<Vertex> iterator = instance.getVerticesWithId(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(Lists.newArrayList(iterator), containsInAnyOrder(vertex1, vertex2));
  }

  @Test
  public void getVerticesWithIdReturnsAnEmptyIteratorWhenNoneAreFound() {
    // setup
    anEmptyVertexSearchResult() //
        .forType(DOMAIN_ENTITY_TYPE) //
        .forId(ID) //
        .foundInDatabase(dbMock);

    // action
    Iterator<Vertex> iterator = instance.getVerticesWithId(DOMAIN_ENTITY_TYPE, ID);

    // verify
    assertThat(iterator, is(notNullValue()));
    assertThat(Iterators.size(iterator), is(0));
  }

  /* ************************************************************
   * Edge
   * ************************************************************/

  @Test
  public void duplicateEdgeDelegatesToEdgeDuplicator() {
    // setup
    Edge edge = anEdge().build();

    // action
    instance.duplicate(edge);

    // verify
    verify(edgeDuplicator).duplicate(edge);
  }

  @Test
  public void findEdgesByPropertyReturnsAnIteratorWithTheLatestEdges() {
    // setup
    Edge latestEdgeWithId = anEdge().withID(ID).withRev(FIRST_REVISION).build();
    Edge latestEdgeWithId2 = anEdge().withID(ID2).withRev(SECOND_REVISION).build();
    anEdgeSearchResult()//
        .forProperty(PROPERTY_NAME, PROPERTY_VALUE)//
        .containsEdge(latestEdgeWithId)//
        .andEdge(latestEdgeWithId2)//
        .andEdge(anEdge().withID(ID2).withRev(FIRST_REVISION).build())//
        .foundInDatabase(dbMock);

    // action
    Iterator<Edge> edges = instance.findEdgesByProperty(RELATION_TYPE, PROPERTY_NAME, PROPERTY_VALUE);

    // verify
    assertThat(Lists.newArrayList(edges), containsInAnyOrder(latestEdgeWithId, latestEdgeWithId2));
  }

  @Test
  public void findEdgesByPropertyReturnsAnEmptyIteratorWhenNoEdgesAreFound() {
    // setup
    anEmptyEdgeSearchResult().forProperty(PROPERTY_NAME, PROPERTY_VALUE).foundInDatabase(dbMock);

    // action
    Iterator<Edge> edges = instance.findEdgesByProperty(RELATION_TYPE, PROPERTY_NAME, PROPERTY_VALUE);

    // verify
    assertThat(Iterators.size(edges), is(0));
  }

  @Test
  public void findEdgesBySourceReturnsTheLastestOutgoingEdgesOfTheLatestSourceVertex() {
    // setup
    Edge latestEdge1 = anEdge().withID(ID).withRev(SECOND_REVISION).build();
    Edge notLatestEdge1 = anEdge().withID(ID).withRev(FIRST_REVISION).build();
    Edge latestEdge2 = anEdge().withID(ID2).withRev(FIRST_REVISION).build();
    Vertex latestVertexWithEdges = aVertex()//
        .withOutgoingEdge(latestEdge1)//
        .withOutgoingEdge(notLatestEdge1)//
        .withOutgoingEdge(latestEdge2)//
        .build();
    Vertex notLatestVertex = aVertex() //
        .withIncomingEdgeWithLabel(VERSION_OF)//
        .withOutgoingEdge(anEdge().build())//
        .build();
    aVertexSearchResult()//
        .forId(ID)//
        .containsVertex(latestVertexWithEdges)//
        .andVertex(notLatestVertex) //
        .foundInDatabase(dbMock);

    // action
    Iterator<Edge> edges = instance.findEdgesBySource(RELATION_TYPE, ID);

    // verify
    assertThat(Lists.newArrayList(edges), containsInAnyOrder(latestEdge1, latestEdge2));
  }

  @Test
  public void findEdgesBySourceReturnsAnEmptyIteratorIfTheSourceVertexCannotBeFound() {
    // setup
    anEmptyVertexSearchResult().forId(ID).foundInDatabase(dbMock);

    // action
    Iterator<Edge> edges = instance.findEdgesBySource(RELATION_TYPE, ID);

    // verify
    assertThat(Iterators.size(edges), is(0));
  }

  @Test
  public void findEdgesBySourceReturnsAnEmptyIteratorIfTheSourceVertexHasNoOutgoingEdges() {
    // setup
    Vertex latestVertexWithoutEdges = aVertex().build();
    aVertexSearchResult().containsVertex(latestVertexWithoutEdges).forId(ID).foundInDatabase(dbMock);;

    // action
    Iterator<Edge> edges = instance.findEdgesBySource(RELATION_TYPE, ID);

    // verify
    assertThat(Iterators.size(edges), is(0));
  }

  @Test
  public void findEdgesBySourceReturnsAnEmptyIteratorIfLatestTheSourceVertexHasNoOutgoingEdges() {
    // setup
    Vertex latestVertexWithout = aVertex().build();
    Vertex notlatestVertexWithEdges = aVertex()//
        .withIncomingEdgeWithLabel(VERSION_OF)//
        .withOutgoingEdge(anEdge().build())//
        .build();
    aVertexSearchResult().forId(ID) //
        .containsVertex(latestVertexWithout) //
        .andVertex(notlatestVertexWithEdges) //
        .foundInDatabase(dbMock);

    // action
    Iterator<Edge> edges = instance.findEdgesBySource(RELATION_TYPE, ID);

    // verify
    assertThat(Iterators.size(edges), is(0));
  }

  @Test
  public void findEdgesByTargeReturnsTheIncomingEdgesOfTheTargetVertex() {
    // setup
    Edge latestEdge1 = anEdge().withID(ID).withRev(SECOND_REVISION).build();
    Edge notLatestEdge1 = anEdge().withID(ID).withRev(FIRST_REVISION).build();
    Edge latestEdge2 = anEdge().withID(ID2).withRev(FIRST_REVISION).build();
    Vertex latestVertexWithEdges = aVertex()//
        .withIncomingEdge(latestEdge1)//
        .withIncomingEdge(notLatestEdge1)//
        .withIncomingEdge(latestEdge2)//
        .build();
    Vertex notLatestVertex = aVertex() //
        .withIncomingEdgeWithLabel(VERSION_OF)//
        .withIncomingEdge(anEdge().build())//
        .build();
    aVertexSearchResult()//
        .forId(ID)//
        .containsVertex(latestVertexWithEdges)//
        .andVertex(notLatestVertex) //
        .foundInDatabase(dbMock);

    // action
    Iterator<Edge> edges = instance.findEdgesByTarget(RELATION_TYPE, ID);

    // verify
    assertThat(Lists.newArrayList(edges), containsInAnyOrder(latestEdge1, latestEdge2));
  }

  @Test
  public void findEdgesByTargetReturnsAnEmptyIteratorIfTheTargetVertexCannotBeFound() {
    // setup
    anEmptyVertexSearchResult().forId(ID).foundInDatabase(dbMock);

    // action
    Iterator<Edge> edges = instance.findEdgesByTarget(RELATION_TYPE, ID);

    // verify
    assertThat(Iterators.size(edges), is(0));
  }

  @Test
  public void findEdgesByTargetReturnsAnEmptyIteratorIfTheTargetVertexHasNoIncomingEdges() {
    // setup
    Vertex latestVertexWithoutEdges = aVertex().build();
    aVertexSearchResult().containsVertex(latestVertexWithoutEdges).forId(ID).foundInDatabase(dbMock);;

    // action
    Iterator<Edge> edges = instance.findEdgesByTarget(RELATION_TYPE, ID);

    // verify
    assertThat(Iterators.size(edges), is(0));
  }

  @Test
  public void findEdgesByTargetReturnsAnEmptyIteratorIfTheLatestTargetVertexHasNoIncomingEdges() {
    // setup
    Vertex latestVertexWithout = aVertex().build();
    Vertex notlatestVertexWithEdges = aVertex()//
        .withIncomingEdgeWithLabel(VERSION_OF)//
        .withIncomingEdge(anEdge().build())//
        .build();
    aVertexSearchResult().forId(ID) //
        .containsVertex(latestVertexWithout) //
        .andVertex(notlatestVertexWithEdges) //
        .foundInDatabase(dbMock);

    // action
    Iterator<Edge> edges = instance.findEdgesByTarget(RELATION_TYPE, ID);

    // verify
    assertThat(Iterators.size(edges), is(0));
  }

  @Test
  public void getEdgeWithRevisionReturnsTheEdgeWithACertainRevision() {
    // setup
    Edge edge = anEdge().build();
    anEdgeSearchResult() //
        .forId(ID) //
        .forProperty(REVISION_PROPERTY_NAME, FIRST_REVISION) //
        .containsEdge(edge) //
        .foundInDatabase(dbMock);

    // action
    Edge foundEdge = instance.getEdgeWithRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(foundEdge, is(sameInstance(edge)));
  }

  @Test
  public void getEdgeWithRevisionReturnsNullIfTheEdgeIsNotFound() {
    // setup
    anEmptyEdgeSearchResult() //
        .forId(ID) //
        .forProperty(REVISION_PROPERTY_NAME, FIRST_REVISION) //
        .foundInDatabase(dbMock);

    // action
    Edge foundEdge = instance.getEdgeWithRevision(RELATION_TYPE, ID, FIRST_REVISION);

    // verify
    assertThat(foundEdge, is(nullValue()));

  }

  @Test
  public void findEdgesWithoutPropertyReturnsVerticesDoNotContainACertainProperty() {
    // setup
    Edge edge1 = anEdge().build();
    Edge edge2 = anEdge().build();
    anEdgeSearchResult()//
        .withoutProperty(PROPERTY_NAME)//
        .containsEdge(edge1) //
        .containsEdge(edge2) //
        .foundInDatabase(dbMock);

    // action
    Iterator<Edge> vertices = instance.findEdgesWithoutProperty(RELATION_TYPE, PROPERTY_NAME);

    // verify
    assertThat(Lists.newArrayList(vertices), containsInAnyOrder(edge1, edge2));
  }

  @Test
  public void findEdgesWithoutPropertyReturnsAnEmptyIteratorWhenNoneAreFound() {
    // setup
    anEmptyEdgeSearchResult().withoutProperty(PROPERTY_NAME).foundInDatabase(dbMock);

    // action
    Iterator<Edge> vertices = instance.findEdgesWithoutProperty(RELATION_TYPE, PROPERTY_NAME);

    // verify
    assertThat(Iterators.size(vertices), is(0));
  }

  @Test
  public void getLatestEdgeByIdReturnsTheEdgeWithTheHighestRevisionForACertainId() {
    // setup
    Edge edgeWithHighestRevision = anEdge().withRev(THIRD_REVISION).build();
    anEdgeSearchResult().forId(ID)//
        .containsEdge(anEdge().withRev(FIRST_REVISION).build())//
        .andEdge(edgeWithHighestRevision)//
        .andEdge(anEdge().withRev(SECOND_REVISION).build())//
        .foundInDatabase(dbMock);

    // action
    Edge foundEdge = instance.getLatestEdgeById(RELATION_TYPE, ID);

    // verify
    assertThat(foundEdge, is(sameInstance(edgeWithHighestRevision)));

  }

  @Test
  public void getLatestEdgeByIdReturnsNullIfNoEdgesAreFound() {
    // setup
    anEmptyEdgeSearchResult().forId(ID).foundInDatabase(dbMock);

    // action
    Edge foundEdge = instance.getLatestEdgeById(RELATION_TYPE, ID);

    // verify
    assertThat(foundEdge, is(nullValue()));
  }

  @Test
  public void findLatestEdgesBuildsGraphQueryFromATinkerPopQueryAndReturnsTheLatestEdgesFromTheResult() {
    // setup
    GraphQuery graphQuery = mock(GraphQuery.class);

    Edge latestEdge1 = anEdge().withID(ID).withRev(SECOND_REVISION).build();
    Edge latestEdge2 = anEdge().withID(ID2).withRev(FIRST_REVISION).build();

    anEdgeSearchResult()//
        .containsEdge(anEdge().withID(ID).withRev(FIRST_REVISION).build())//
        .andEdge(latestEdge1)//
        .andEdge(latestEdge2)//
        .foundByGraphQuery(graphQuery);

    TinkerPopQuery query = aQuery().createsGraphQueryForDB(dbMock, graphQuery).build();

    // action
    Iterator<Edge> edges = instance.findLatestEdges(query);

    // verify
    assertThat(Lists.newArrayList(edges), containsInAnyOrder(latestEdge1, latestEdge2));
  }

  @Test
  public void findLatestEdgesReturnsAnEmptyIteratorWhenNoEdgesAreFound() {
    // setup
    GraphQuery graphQuery = mock(GraphQuery.class);
    anEmptyEdgeSearchResult().foundByGraphQuery(graphQuery);

    TinkerPopQuery query = aQuery().createsGraphQueryForDB(dbMock, graphQuery).build();

    // action
    Iterator<Edge> edges = instance.findLatestEdges(query);

    // verify
    assertThat(Iterators.size(edges), is(0));
  }

  @Test
  public void getLatestEdgesOfReturnsOnlyTheLatestVersions() {
    // setup
    Edge edgeWithLatestRev1 = anEdge().withID(ID).withRev(SECOND_REVISION).build();
    Edge edgeWithLatestRev2 = anEdge().withID(ID2).withRev(SECOND_REVISION).build();
    anEdgeSearchResult() //
        .containsEdge(anEdge().withID(ID).withRev(FIRST_REVISION).build()) //
        .andEdge(edgeWithLatestRev1) //
        .andEdge(anEdge().withID(ID2).withRev(FIRST_REVISION).build()) //
        .andEdge(edgeWithLatestRev2) //
        .foundInDatabase(dbMock);

    // action
    Iterator<Edge> actualEdges = instance.getLatestEdgesOf(RELATION_TYPE);

    // verify
    ArrayList<Edge> edgesList = Lists.newArrayList(actualEdges);
    assertThat(edgesList.size(), is(2));
    assertThat(edgesList, containsInAnyOrder(edgeWithLatestRev1, edgeWithLatestRev2));
  }

  @Test
  public void getLatestEdgesOfReturnsAnEmptyIteratorWhenNoEdgesAreFound() {
    // setup
    anEmptyEdgeSearchResult().foundInDatabase(dbMock);

    // action
    Iterator<Edge> actualEdges = instance.getLatestEdgesOf(RELATION_TYPE);

    // verify
    assertThat(actualEdges.hasNext(), is(false));
  }
}
