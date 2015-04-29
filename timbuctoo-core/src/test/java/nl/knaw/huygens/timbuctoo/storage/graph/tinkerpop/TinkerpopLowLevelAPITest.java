package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.model.Entity.REVISION_PROPERTY_NAME;
import static nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType.VERSION_OF;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeMockBuilder.anEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeSearchResultBuilder.anEdgeSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.EdgeSearchResultBuilder.anEmptyEdgeSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexSearchResultBuilder.aVertexSearchResult;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexSearchResultBuilder.anEmptyVertexSearchResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
import com.tinkerpop.blueprints.Vertex;

public class TinkerpopLowLevelAPITest {
  private static final int SECOND_REVISION = 2;
  private static final int THIRD_REVISION = 3;
  private static final Class<SubARelation> RELATION_TYPE = SubARelation.class;
  private static final int FIRST_REVISION = 1;
  private static final Class<SubADomainEntity> DOMAIN_ENTITY_TYPE = SubADomainEntity.class;
  private static final Class<TestSystemEntityWrapper> SYSTEM_ENTITY_TYPE = TestSystemEntityWrapper.class;
  private static final String ID = "id";
  private Graph dbMock;
  private TinkerpopLowLevelAPI instance;
  private VertexDuplicator vertexDuplicator;
  private EdgeDuplicator edgeDuplicator;

  @Before
  public void setup() {
    edgeDuplicator = mock(EdgeDuplicator.class);
    vertexDuplicator = mock(VertexDuplicator.class);
    dbMock = mock(Graph.class);
    instance = new TinkerpopLowLevelAPI(dbMock, vertexDuplicator, edgeDuplicator);
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
  public void getLatestVerticesFiltersReturnsOnlyTheLatestVersions() {
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
  public void getLatestVerticesReturnsAnEmptyIteratorWhenNoVerticesAreFound() {
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
}
